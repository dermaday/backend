package com.likelion.dermaday.api.member.dto.response;

import com.likelion.dermaday.api.member.domain.MemberRole;

public record MemberAuthenticationResponse(
        Long memberId,
        String displayName,
        MemberRole role
) {
}
