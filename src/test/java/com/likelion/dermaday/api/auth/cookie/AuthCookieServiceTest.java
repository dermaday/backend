package com.likelion.dermaday.api.auth.cookie;

import com.likelion.dermaday.api.auth.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthCookieServiceTest {

    @Test
    void writesSecureHttpOnlyCookieForTwentyFourHours() {
        AuthCookieService service = new AuthCookieService(properties());
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.addAccessToken(response, "access-token");

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.contains("DERMADAY_ACCESS_TOKEN=access-token"));
        assertTrue(cookie.contains("Max-Age=86400"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
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
