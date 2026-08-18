package com.likelion.dermaday.api.auth.principal;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class MemberJwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final MemberPrincipal principal;

    public MemberJwtAuthenticationToken(
            Jwt jwt,
            MemberPrincipal principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.jwt = jwt;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public MemberPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.id().toString();
    }

    public Jwt getJwt() {
        return jwt;
    }
}
