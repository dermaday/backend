package com.likelion.dermaday.api.member.dto.response;

import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;

public record MemberMeResponse(
        Long id,
        String displayName,
        MemberRole role,
        OAuthProvider provider
) {
}
