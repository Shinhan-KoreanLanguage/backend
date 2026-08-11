package com.daehanforeigner.capstone.domain.user.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 액세스 토큰 재발급 요청. 프론트가 보관 중인 리프레시 토큰을 보낸다.
@Schema(description = "토큰 재발급 요청")
public record TokenReissueRequestDTO(
        @Schema(description = "로그인 시 발급받아 보관 중인 리프레시 토큰",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.yyyyy")
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
