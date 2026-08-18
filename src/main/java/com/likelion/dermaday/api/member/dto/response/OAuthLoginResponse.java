package com.likelion.dermaday.api.member.dto.response;

import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;

public record OAuthLoginResponse(
        Long memberId,
        String displayName,
        MemberRole role,
        OAuthProvider provider
) {
}
