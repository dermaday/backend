package com.likelion.dermaday.api.auth.jwt;

import com.likelion.dermaday.api.auth.config.AppProperties;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    public static final String ISSUER = "dermaday";
    public static final String ROLE_CLAIM = "role";
    public static final String PROVIDER_CLAIM = "provider";

    private final JwtEncoder jwtEncoder;
    private final AppProperties properties;

    public String createAccessToken(Long memberId, MemberRole role, OAuthProvider provider) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.jwt().accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(memberId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(ROLE_CLAIM, role.name())
                .claim(PROVIDER_CLAIM, provider.name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
