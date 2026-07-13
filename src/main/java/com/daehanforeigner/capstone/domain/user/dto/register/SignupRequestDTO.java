package com.daehanforeigner.capstone.domain.user.dto.register;

public record SignupRequestDTO(
        String email,
        String password,
        String nickname,
        String nativeLanguage
) {
}
