package com.daehanforeigner.capstone.domain.user.controller.auth;

import com.daehanforeigner.capstone.domain.social_account.dto.SocialLoginDTO;
import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.domain.social_account.service.SocialAuthService;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.TokenReissueRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.register.SignupRequestDTO;
import com.daehanforeigner.capstone.domain.user.service.auth.UserAuthService;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.oauth.client.OAuthClientFactory;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserAuthService userService;
    private final SocialAuthService socialAuthService;
    private final OAuthClientFactory oAuthClientFactory;

    @PostMapping("/signup")
    public ResponseEntity<RsData<String>> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        userService.signup(signupRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(RsData.success("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<RsData<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = userService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }

    // 로그아웃 — 인증된 사용자의 리프레시 토큰 제거
    @PostMapping("/logout")
    public ResponseEntity<RsData<String>> logout(@AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok(RsData.success("로그아웃 되었습니다."));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<RsData<String>> deleteAccount(@AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ResponseEntity.ok(RsData.success("회원 탈퇴가 완료되었습니다."));
    }

    // 액세스 토큰 재발급 — 만료된 액세스 토큰 상태에서 호출하므로 리프레시 토큰만 받음
    @PostMapping("/reissue")
    public ResponseEntity<RsData<LoginResponseDTO>> reissue(@Valid @RequestBody TokenReissueRequestDTO request) {
        LoginResponseDTO loginResponseDTO = userService.reissue(request);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }

    // 소셜 로그인 시작점 — 브라우저(또는 프론트)가 이 주소로 접속하면
    // 해당 소셜 로그인 페이지로 바로 리다이렉트됨.
    // 프론트가 client_id를 알 필요가 없어지는 효과도 있음
    @GetMapping("/social/{provider}/login-url")
    public ResponseEntity<Void> socialLoginUrl(@PathVariable("provider") String provider) { // 이름 명시: -parameters 플래그 없이도 매핑되게
        String loginUrl = oAuthClientFactory.getClient(parseProvider(provider)).generateLoginUrl();
        return ResponseEntity.status(HttpStatus.FOUND)          // 302 리다이렉트
                .header(HttpHeaders.LOCATION, loginUrl)
                .build();
    }

    // 소셜 로그인 — /social/google, /social/facebook 두 경로를 이 메서드 하나로 처리
    @PostMapping("/social/{provider}")
    public ResponseEntity<RsData<LoginResponseDTO>> socialLogin(
            @PathVariable("provider") String provider,
            @Valid @RequestBody SocialLoginDTO socialLoginDTO) {
        LoginResponseDTO loginResponseDTO = socialAuthService.socialLogin(parseProvider(provider), socialLoginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }

    // URL 문자열("google") → enum(GOOGLE) 변환. google/facebook 외의 값이면 400
    private Provider parseProvider(String provider) {
        try {
            return Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }
    }
}