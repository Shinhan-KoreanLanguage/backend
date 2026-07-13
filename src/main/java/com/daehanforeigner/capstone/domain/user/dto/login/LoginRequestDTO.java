package com.daehanforeigner.capstone.domain.user.dto.login;

public record LoginRequestDTO(
        String email,
        String password
) {
}
