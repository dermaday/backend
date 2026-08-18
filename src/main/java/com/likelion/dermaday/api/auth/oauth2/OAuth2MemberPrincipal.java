package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public record OAuth2MemberPrincipal(
        Long memberId,
        String displayName,
        MemberRole role,
        OAuthProvider provider,
        Map<String, Object> attributes,
        Collection<? extends GrantedAuthority> authorities
) implements OAuth2User {

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return memberId.toString();
    }
}
