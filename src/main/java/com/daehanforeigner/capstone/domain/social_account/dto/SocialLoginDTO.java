package com.daehanforeigner.capstone.domain.social_account.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginDTO(
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code
) {
}
