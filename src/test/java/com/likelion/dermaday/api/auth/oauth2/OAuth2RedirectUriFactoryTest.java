package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.auth.config.AppProperties;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuth2RedirectUriFactoryTest {

    private final OAuth2RedirectUriFactory factory = new OAuth2RedirectUriFactory(properties());

    @Test
    void createsProviderSpecificSuccessRedirect() {
        assertEquals(
                "https://dermaday.me/auth/kakao/callback",
                factory.success(OAuthProvider.KAKAO)
        );
        assertEquals(
                "https://dermaday.me/auth/naver/callback",
                factory.success(OAuthProvider.NAVER)
        );
    }

    @Test
    void appendsOnlySafeFailureCode() {
        assertEquals(
                "https://dermaday.me/auth/naver/callback?error=oauth2_login_failed",
                factory.failure(OAuthProvider.NAVER, "oauth2_login_failed")
        );
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
