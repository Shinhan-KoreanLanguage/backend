package com.daehanforeigner.capstone.domain.user.controller;

import com.daehanforeigner.capstone.domain.social_account.dto.SocialLoginDTO;
import com.daehanforeigner.capstone.domain.social_account.entity.Provider;
import com.daehanforeigner.capstone.domain.social_account.service.SocialAuthService;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.register.SignupRequestDTO;
import com.daehanforeigner.capstone.domain.user.service.UserService;
import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SocialAuthService socialAuthService;

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

    // 소셜 로그인 — /social/google, /social/facebook 두 경로를 이 메서드 하나로 처리
    @PostMapping("/social/{provider}")
    public ResponseEntity<RsData<LoginResponseDTO>> socialLogin(
            @PathVariable String provider,
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