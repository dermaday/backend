package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.member.domain.OAuthProvider;

public record OAuthProfile(
        OAuthProvider provider,
        String providerUserId,
        String displayName
) {
}
