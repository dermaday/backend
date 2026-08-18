package com.likelion.dermaday.api.member.dto.request;

import com.likelion.dermaday.api.member.domain.OAuthProvider;

public record OAuthLoginRequest(
        OAuthProvider provider,
        String providerUserId,
        String displayName
) {
}
