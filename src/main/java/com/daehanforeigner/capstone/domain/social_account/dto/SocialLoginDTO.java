package com.daehanforeigner.capstone.domain.social_account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginDTO(
        @Schema(description = """
                소셜 인증 후 redirect_uri로 전달받은 인가 코드.
                일회용이라 재사용하면 실패합니다.
                """,
                example = "4/0AeanS0b7X...")
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code
) {
}
