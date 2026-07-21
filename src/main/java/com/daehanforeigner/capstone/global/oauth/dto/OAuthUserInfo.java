package com.daehanforeigner.capstone.global.oauth.dto;

import com.daehanforeigner.capstone.domain.social_account.entity.Provider;

public record OAuthUserInfo(
        Provider provider,
        String providerUserId,
        String email,
        String nickname
) {
}
