package com.daehanforeigner.capstone.domain.social_account.service;

import com.daehanforeigner.capstone.domain.social_account.dto.SocialLoginDTO;
import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.domain.social_account.entity.SocialAccount;
import com.daehanforeigner.capstone.domain.social_account.repository.SocialAccountRepository;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.Status;
import com.daehanforeigner.capstone.domain.user.entity.User;
import com.daehanforeigner.capstone.domain.user.repository.UserRepository;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.jwt.JwtProvider;
import com.daehanforeigner.capstone.global.oauth.client.OAuthClientFactory;
import com.daehanforeigner.capstone.global.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    // 구글/페이스북 클라이언트를 직접 알지 않고 Factory에게 배정을 맡김
    // → 이 파일에는 구글/페이스북 고유 로직이 전혀 없음
    private final OAuthClientFactory oAuthClientFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 소셜 로그인 전체 흐름:
    // 인가코드 → 소셜 사용자 정보 → 회원 찾기(없으면 자동가입) → 우리 JWT 발급
    @Transactional
    public LoginResponseDTO socialLogin(Provider provider, SocialLoginDTO socialLoginDTO) {

        // [1] 담당 클라이언트가 소셜 서버와 2단계 통신(토큰 교환 + 정보 조회)까지 완료
        OAuthUserInfo userInfo = oAuthClientFactory.getClient(provider)
                .getUserInfo(normalizeCode(socialLoginDTO.code()));

        // [2] 이 소셜 계정으로 가입된 회원 조회.
        // 식별 키는 (provider, providerUserId) 조합뿐 — 이메일로 찾지 않음
        // (구글 이메일과 페이스북 이메일이 서로 달라서 이메일은 본인 판단 근거가 못 됨 — 멘토 조언)
        // 있으면 → 그 User 사용 / 없으면 → 자동 회원가입 (소셜 최초 로그인 = 가입)
        User user = socialAccountRepository
                .findByProviderAndProviderUserId(provider, userInfo.providerUserId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> registerSocialUser(userInfo));

        // [3] 탈퇴 회원 차단 (로컬 로그인과 동일한 정책)
        if (user.getStatus() == Status.WITHDRAWN) {
            throw new CustomException(ErrorCode.WITHDRAWN_USER);
        }

        // [4] 우리 서비스의 JWT 발급 — 여기부터는 로컬 login()과 완전히 동일
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        user.updateRefreshToken(refreshToken); // 변경 감지(dirty checking)로 UPDATE

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    // 소셜 최초 로그인 시 자동 회원가입: User + SocialAccount를 한 트랜잭션에서 함께 생성
    private User registerSocialUser(OAuthUserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.email())              // 표시용 (페이스북은 null 가능 — 전화번호 가입 계정)
                .nickname(resolveNickname(userInfo))  // 이름을 못 받은 경우 기본 닉네임 생성
                .role(Role.USER)
                .status(Status.ACTIVE)
                // password 없음(소셜 회원) — 로컬 로그인의 null 체크가 방어
                // nativeLanguage 없음 — 추후 "추가 정보 입력" API에서 보완
                .build();
        userRepository.save(user);

        socialAccountRepository.save(SocialAccount.builder()
                .user(user)
                .provider(userInfo.provider())
                .providerUserId(userInfo.providerUserId())
                .build());

        return user;
    }

    // 인가 코드 정리 — 페이스북이 리다이렉트 URL 끝에 붙이는 #_=_ 장식이
    // code에 딸려 들어오는 실수(사람/프론트 모두 흔함)를 방어.
    // 정상 인가 코드에는 #이 절대 없으므로 # 이후는 잘라도 안전
    private String normalizeCode(String rawCode) {
        int fragmentIndex = rawCode.indexOf('#');
        return fragmentIndex == -1 ? rawCode : rawCode.substring(0, fragmentIndex);
    }

    // 외부 API 응답을 100% 신뢰하지 않는 방어 코드 — 이름이 비어 오면 기본 닉네임으로 대체
    private String resolveNickname(OAuthUserInfo userInfo) {
        if (userInfo.nickname() != null && !userInfo.nickname().isBlank()) {
            return userInfo.nickname();
        }
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }
}