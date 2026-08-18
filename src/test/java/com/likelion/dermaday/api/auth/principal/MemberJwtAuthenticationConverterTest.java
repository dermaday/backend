package com.likelion.dermaday.api.auth.principal;

import com.likelion.dermaday.api.auth.jwt.JwtTokenService;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.dto.response.MemberAuthenticationResponse;
import com.likelion.dermaday.api.member.service.MemberAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberJwtAuthenticationConverterTest {

    private final MemberAuthenticationService memberAuthenticationService = mock(MemberAuthenticationService.class);
    private final MemberJwtAuthenticationConverter converter =
            new MemberJwtAuthenticationConverter(memberAuthenticationService);

    @Test
    void createsPrincipalFromActiveMember() {
        MemberAuthenticationResponse member =
                new MemberAuthenticationResponse(1L, "사용자", MemberRole.USER);
        when(memberAuthenticationService.findActiveMember(1L)).thenReturn(Optional.of(member));

        MemberJwtAuthenticationToken authentication =
                (MemberJwtAuthenticationToken) converter.convert(jwt());

        assertEquals(1L, authentication.getPrincipal().id());
        assertEquals("사용자", authentication.getPrincipal().displayName());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void rejectsWithdrawnMemberEvenWhenJwtIsValid() {
        when(memberAuthenticationService.findActiveMember(1L)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> converter.convert(jwt()));
    }

    private Jwt jwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuer(JwtTokenService.ISSUER)
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim(JwtTokenService.PROVIDER_CLAIM, "KAKAO")
                .build();
    }
}
