package com.likelion.dermaday.api.auth.principal;

import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;

public record MemberPrincipal(
        Long id,
        String displayName,
        MemberRole role,
        OAuthProvider provider
) {
}
