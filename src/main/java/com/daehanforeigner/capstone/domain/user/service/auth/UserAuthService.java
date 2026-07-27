package com.daehanforeigner.capstone.domain.user.service.auth;

import com.daehanforeigner.capstone.domain.user.dto.login.LoginRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.TokenReissueRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.register.SignupRequestDTO;
import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.Status;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    // 회원가입 관련
    @Transactional
    public void signup(SignupRequestDTO signupRequestDTO) {

        if (userRepository.existsByEmail(signupRequestDTO.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(signupRequestDTO.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        NativeLanguage nativeLanguage = parseNativeLanguage(signupRequestDTO.nativeLanguage());

        User user = User.builder()
                .email(signupRequestDTO.email())
                .password(passwordEncoder.encode(signupRequestDTO.password()))
                .nickname(signupRequestDTO.nickname())
                .nativeLanguage(nativeLanguage)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(user);
    }

    // 로그인 관련
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getStatus() == Status.WITHDRAWN) {
            throw new CustomException(ErrorCode.WITHDRAWN_USER);
        }

        if (user.getPassword() == null
                || !passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        user.updateRefreshToken(refreshToken);

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    // 공통: userId로 회원 조회 (없으면 예외) — UserProfileService와 동일한 방식으로 통일
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 로그아웃 — DB의 리프레시 토큰 제거 → 이후 재발급 차단
    @Transactional
    public void logout(Long userId) {
        User user = findUser(userId);
        user.updateRefreshToken(null);
    }

    // 회원탈퇴 (소프트 딜리트) DB값 지우지 않고 탈퇴 표시 14일 이후에는 DB 자동 삭제
    @Transactional
    public void withdraw(Long userId) {
        User user = findUser(userId);
        user.withdraw(); // User 엔티티에서 상태를 WITHDRAWN으로 변경하고 deletedAt을 현재 시간으로 설정
    }

    // 액세스 토큰 재발급
    @Transactional(readOnly = true)
    public LoginResponseDTO reissue(TokenReissueRequestDTO request) {
        String refreshToken = request.refreshToken();

        // 1. 리프레시 토큰 자체가 유효한가 (서명·만료 검증)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 토큰에서 userId 추출 → 회원 조회
        Long userId = jwtProvider.getUserId(refreshToken);
        User user = findUser(userId);

        // 3. DB에 저장된 리프레시 토큰과 일치하는가
        //    (로그아웃/탈퇴로 null이거나, 다른 기기 로그인으로 교체됐으면 불일치 → 거부)
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 새 액세스 토큰 발급 (리프레시는 그대로 유지)
        String newAccessToken = jwtProvider.createAccessToken(userId);
        return new LoginResponseDTO(newAccessToken, refreshToken);
    }

    // 모국어 문자열을 NativeLanguage enum으로 변환하는 메서드
    private NativeLanguage parseNativeLanguage(String value) {
        try {
            return NativeLanguage.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}