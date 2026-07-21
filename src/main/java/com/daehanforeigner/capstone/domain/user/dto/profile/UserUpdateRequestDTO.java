package com.daehanforeigner.capstone.domain.user.dto.profile;

import com.daehanforeigner.capstone.domain.user.entity.NativeLanguage;

public record UserUpdateRequestDTO(
        String nickname,
        NativeLanguage nativeLanguage,
        String profileImageUrl
) {
}
