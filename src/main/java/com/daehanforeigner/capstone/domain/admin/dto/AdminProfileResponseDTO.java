package com.daehanforeigner.capstone.domain.admin.dto;

import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.User;

public record AdminProfileResponseDTO(
        Long userId,
        String email,
        String nickname,
        Role role
) {
    public static AdminProfileResponseDTO from(User user) {
        return new AdminProfileResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole());
    }
}
