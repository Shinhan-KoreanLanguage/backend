package com.daehanforeigner.capstone.domain.user.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequestDTO(
        @Schema(description = "이메일 (로그인 ID로 사용)", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호 (8~20자)", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        String password,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "모국어 — 대소문자 구분 없이 보내도 됩니다 (kr → KR)",
                example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
        String nativeLanguage
) {
}
