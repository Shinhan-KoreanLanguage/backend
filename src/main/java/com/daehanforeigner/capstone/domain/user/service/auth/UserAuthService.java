package com.daehanforeigner.capstone.domain.user.service.auth;

import com.daehanforeigner.capstone.domain.user.dto.login.LoginRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
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

    // 모국어 문자열을 NativeLanguage enum으로 변환하는 메서드
    private NativeLanguage parseNativeLanguage(String value) {
        try {
            return NativeLanguage.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }

    //
}