package com.daehanforeigner.capstone.domain.user.dto.login;

import jakarta.validation.constraints.NotBlank;

// 액세스 토큰 재발급 요청. 프론트가 보관 중인 리프레시 토큰을 보낸다.
public record TokenReissueRequestDTO(
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
