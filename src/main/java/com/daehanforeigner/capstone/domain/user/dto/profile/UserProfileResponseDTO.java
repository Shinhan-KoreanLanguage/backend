package com.daehanforeigner.capstone.domain.user.dto.profile;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;
import com.daehanforeigner.capstone.domain.user.entity.Role;
import com.daehanforeigner.capstone.domain.user.entity.User;

// 마이페이지 조회 시 사용되는 DTO
public record UserProfileResponseDTO(
        Long userId,
        String nickname,
        String email,
        NativeLanguage nativeLanguage,
        Role role,
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
