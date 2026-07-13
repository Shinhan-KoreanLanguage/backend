package com.daehanforeigner.capstone.domain.user.dto.login;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken
) {
}
