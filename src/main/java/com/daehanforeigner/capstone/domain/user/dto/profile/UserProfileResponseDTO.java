package com.daehanforeigner.capstone.domain.user.dto.profile;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

// 마이페이지 조회 시 사용되는 DTO
@Schema(description = "내 정보 응답")
public record UserProfileResponseDTO(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "모국어", example = "EN", allowableValues = {"KR", "EN", "JP", "CN"})
        NativeLanguage nativeLanguage,

        @Schema(description = "권한 — ADMIN이면 관리자 화면 접근 가능",
                example = "USER", allowableValues = {"USER", "ADMIN"})
        Role role,

        @Schema(description = "프로필 이미지 URL — 미설정 회원은 기본 이미지 URL이 담깁니다 (null 아님)",
                example = "http://localhost:8080/images/profile/uuid.png")
        String profileImageUrl // 프로필 이미지 URL
) {
    public static UserProfileResponseDTO from(User user, String defaultProfileImageUrl) {
        String imageUrl = user.getProfileImageUrl() != null ? user.getProfileImageUrl() : defaultProfileImageUrl;

        return new UserProfileResponseDTO(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getNativeLanguage(),
                user.getRole(),
                imageUrl
        );
    }
}
