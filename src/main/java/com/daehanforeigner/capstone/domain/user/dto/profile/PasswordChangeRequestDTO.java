package com.daehanforeigner.capstone.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequestDTO(
        @NotBlank(message = "기존 비밀번호를 입력하세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력하세요.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
        String confirmNewPassword
) {
}