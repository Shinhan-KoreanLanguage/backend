package com.daehanforeigner.capstone.domain.user.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청 — 세 필드 모두 필수")
public record PasswordChangeRequestDTO(
        @Schema(description = "현재 사용 중인 비밀번호", example = "password123")
        @NotBlank(message = "기존 비밀번호를 입력하세요.")
        String currentPassword,

        @Schema(description = "새 비밀번호 (8자 이상)", example = "newPassword123")
        @NotBlank(message = "새 비밀번호를 입력하세요.")
        String newPassword,

        @Schema(description = "새 비밀번호 확인 — newPassword와 같아야 합니다", example = "newPassword123")
        @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
        String confirmNewPassword
) {
}
