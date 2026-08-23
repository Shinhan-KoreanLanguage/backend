package com.daehanforeigner.capstone.domain.admin.dto;

import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 정보 응답")
public record AdminProfileResponseDTO(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "admin@example.com")
        String email,

        @Schema(description = "닉네임", example = "관리자")
        String nickname,

        @Schema(description = "권한 — 이 API는 ADMIN만 호출 가능", example = "ADMIN",
                allowableValues = {"USER", "ADMIN"})
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
