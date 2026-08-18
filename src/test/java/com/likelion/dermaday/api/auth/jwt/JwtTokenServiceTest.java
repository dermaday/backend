package com.likelion.dermaday.api.auth.jwt;

import com.likelion.dermaday.api.auth.config.AppProperties;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    @Test
    void createsAndDecodesAccessToken() {
        AppProperties properties = properties();
        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder decoder = config.jwtDecoder(secretKey);
        JwtTokenService service = new JwtTokenService(encoder, properties);

        String token = service.createAccessToken(42L, MemberRole.USER, OAuthProvider.KAKAO);
        Jwt jwt = decoder.decode(token);

        assertEquals("42", jwt.getSubject());
        assertEquals("USER", jwt.getClaimAsString(JwtTokenService.ROLE_CLAIM));
        assertEquals("KAKAO", jwt.getClaimAsString(JwtTokenService.PROVIDER_CLAIM));
        assertTrue(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).equals(Duration.ofHours(24)));
    }

    private AppProperties properties() {
        return new AppProperties(
                "https://dermaday.me",
                "https://api.dermaday.me",
                new AppProperties.Cors(List.of("https://dermaday.me")),
                new AppProperties.Jwt("test-jwt-secret-that-is-at-least-32-bytes-long", Duration.ofHours(24)),
                new AppProperties.AuthCookie("DERMADAY_ACCESS_TOKEN", true, "Lax")
        );
    }
}
