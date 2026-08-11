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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증", description = "회원가입 · 로그인 · 토큰 관리")
public class UserController {

    private final UserAuthService userService;
    private final SocialAuthService socialAuthService;
    private final OAuthClientFactory oAuthClientFactory;

    @Operation(
            summary = "회원가입",
            description = """
                    이메일·비밀번호로 가입합니다. 소셜 가입은 `POST /auth/social/{provider}`를 사용하세요.
                    가입만 처리하며 토큰은 발급되지 않으므로, 가입 후 로그인 API를 호출해야 합니다.
                    """,
            security = {} // 인증 불필요
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 — 이메일 형식, 비밀번호 8~20자 위반 등"),
            @ApiResponse(responseCode = "409", description = "`EMAIL_ALREADY_EXISTS` 이미 가입된 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<RsData<String>> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        userService.signup(signupRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(RsData.success("회원가입이 완료되었습니다."));
    }

    @Operation(
            summary = "로그인",
            description = """
                    이메일·비밀번호로 로그인하고 토큰을 발급받습니다.

                    - `accessToken` (60분) — 이후 모든 요청의 `Authorization: Bearer {token}` 헤더에 넣으세요
                    - `refreshToken` (30일) — 액세스 토큰 만료 시 재발급에 사용하니 보관하세요

                    관리자 여부는 토큰에 담기므로, 권한이 바뀌면 다시 로그인해야 반영됩니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "`INVALID_CREDENTIALS` 이메일 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "403", description = "`WITHDRAWN_USER` 탈퇴한 회원")
    })
    @PostMapping("/login")
    public ResponseEntity<RsData<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = userService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }

    // 로그아웃 — 인증된 사용자의 리프레시 토큰 제거
    @Operation(
            summary = "로그아웃",
            description = """
                    서버에 저장된 리프레시 토큰을 제거합니다.
                    액세스 토큰 자체는 만료 전까지 유효하므로, 프론트에서도 보관 중인 토큰을 함께 지워주세요.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`")
    })
    @PostMapping("/logout")
    public ResponseEntity<RsData<String>> logout(
            // 토큰에서 꺼내는 값이라 문서에 노출하면 프론트가 직접 넣는 값으로 오해함
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok(RsData.success("로그아웃 되었습니다."));
    }

    // 회원 탈퇴
    @Operation(
            summary = "회원 탈퇴",
            description = """
                    탈퇴 처리합니다. 즉시 삭제가 아니라 14일간 보관 후 DB에서 완전히 제거됩니다.
                    탈퇴 후에는 같은 계정으로 로그인할 수 없습니다 (`WITHDRAWN_USER`).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "`TOKEN_EXPIRED` / `TOKEN_INVALID`"),
            @ApiResponse(responseCode = "404", description = "`USER_NOT_FOUND` 존재하지 않는 회원")
    })
    @DeleteMapping("/me")
    public ResponseEntity<RsData<String>> deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ResponseEntity.ok(RsData.success("회원 탈퇴가 완료되었습니다."));
    }

    // 액세스 토큰 재발급 — 만료된 액세스 토큰 상태에서 호출하므로 리프레시 토큰만 받음
    @Operation(
            summary = "액세스 토큰 재발급",
            description = """
                    액세스 토큰이 만료됐을 때(401 `TOKEN_EXPIRED`) 보관 중인 리프레시 토큰으로 재발급합니다.
                    만료된 액세스 토큰 상태에서 호출하므로 **Authorization 헤더는 필요 없습니다.**

                    리프레시 토큰까지 만료됐다면 재로그인시켜야 합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401",
                    description = "`REFRESH_TOKEN_EXPIRED` 리프레시 토큰 만료 (재로그인 필요) / "
                            + "`REFRESH_TOKEN_INVALID` 저장된 토큰과 불일치")
    })
    @PostMapping("/reissue")
    public ResponseEntity<RsData<LoginResponseDTO>> reissue(@Valid @RequestBody TokenReissueRequestDTO request) {
        LoginResponseDTO loginResponseDTO = userService.reissue(request);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }

    // 소셜 로그인 시작점 — 브라우저(또는 프론트)가 이 주소로 접속하면
    // 해당 소셜 로그인 페이지로 바로 리다이렉트됨.
    // 프론트가 client_id를 알 필요가 없어지는 효과도 있음
    @Operation(
            summary = "소셜 로그인 페이지로 이동",
            description = """
                    소셜 로그인 버튼의 링크로 사용하세요. 302로 해당 소셜 로그인 페이지에 리다이렉트합니다.
                    client_id·redirect_uri를 서버가 조립하므로 프론트가 알 필요가 없습니다.

                    **주의:** 이 API는 JSON이 아닌 리다이렉트 응답이라 Swagger UI에서 테스트하기 어렵습니다.
                    브라우저 주소창에 직접 입력해 확인하세요.

                    이후 소셜 인증이 끝나면 설정된 redirect_uri로 인가 코드(`code`)가 전달되고,
                    프론트는 그 코드를 `POST /auth/social/{provider}`로 보내면 됩니다.
                    """,
            security = {}
    )
    @ApiResponse(responseCode = "302", description = "소셜 로그인 페이지로 리다이렉트")
    @GetMapping("/social/{provider}/login-url")
    public ResponseEntity<Void> socialLoginUrl(
            @Parameter(description = "소셜 제공자", example = "google",
                    schema = @Schema(allowableValues = {"google", "facebook"}))
            @PathVariable("provider") String provider) { // 이름 명시: -parameters 플래그 없이도 매핑되게
        String loginUrl = oAuthClientFactory.getClient(parseProvider(provider)).generateLoginUrl();
        return ResponseEntity.status(HttpStatus.FOUND)          // 302 리다이렉트
                .header(HttpHeaders.LOCATION, loginUrl)
                .build();
    }

    // 소셜 로그인 — /social/google, /social/facebook 두 경로를 이 메서드 하나로 처리
    @Operation(
            summary = "소셜 로그인",
            description = """
                    소셜 인증 후 받은 **인가 코드(code)** 를 전달하면 로그인 처리 후 JWT를 반환합니다.
                    가입된 적 없는 계정이면 자동으로 회원가입까지 진행됩니다.

                    - `redirect_uri`는 서버 설정값을 사용하므로 프론트가 보낼 필요 없습니다
                    - 인가 코드는 **일회용**입니다. 같은 코드를 두 번 보내면 실패합니다
                    - 페이스북은 리다이렉트 URL 끝에 `#_=_`가 붙을 수 있는데, 서버에서 제거하므로 그대로 보내도 됩니다
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (신규 계정이면 자동 가입)"),
            @ApiResponse(responseCode = "400",
                    description = "`UNSUPPORTED_SOCIAL_PROVIDER` google·facebook 외의 값 / "
                            + "`INVALID_AUTHORIZATION_CODE` 인가 코드 불일치"),
            @ApiResponse(responseCode = "401", description = "`SOCIAL_AUTHENTICATION_FAILED` 소셜 서버 인증 실패")
    })
    @PostMapping("/social/{provider}")
    public ResponseEntity<RsData<LoginResponseDTO>> socialLogin(
            @Parameter(description = "소셜 제공자", example = "google",
                    schema = @Schema(allowableValues = {"google", "facebook"}))
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
