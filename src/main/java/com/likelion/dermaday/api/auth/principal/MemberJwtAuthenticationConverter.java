package com.likelion.dermaday.api.auth.principal;

import com.likelion.dermaday.api.auth.jwt.JwtTokenService;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.dto.response.MemberAuthenticationResponse;
import com.likelion.dermaday.api.member.service.MemberAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final MemberAuthenticationService memberAuthenticationService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long memberId = parseMemberId(jwt.getSubject());
        MemberAuthenticationResponse member = memberAuthenticationService.findActiveMember(memberId)
                .orElseThrow(() -> new BadCredentialsException("Active member not found"));
        OAuthProvider provider = parseProvider(jwt.getClaimAsString(JwtTokenService.PROVIDER_CLAIM));

        MemberPrincipal principal = new MemberPrincipal(
                member.memberId(),
                member.displayName(),
                member.role(),
                provider
        );

        return new MemberJwtAuthenticationToken(
                jwt,
                principal,
                List.of(new SimpleGrantedAuthority(member.role().authority()))
        );
    }

    private Long parseMemberId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("Invalid member subject", exception);
        }
    }

    private OAuthProvider parseProvider(String provider) {
        try {
            return OAuthProvider.valueOf(provider);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BadCredentialsException("Invalid OAuth2 provider claim", exception);
        }
    }
}
