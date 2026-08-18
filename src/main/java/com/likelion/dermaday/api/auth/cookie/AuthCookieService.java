package com.likelion.dermaday.api.auth.cookie;

import com.likelion.dermaday.api.auth.config.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    private final AppProperties properties;

    public void addAccessToken(HttpServletResponse response, String accessToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(accessToken, properties.jwt().accessTokenTtl()).toString());
    }

    public void clearAccessToken(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString());
    }

    public String cookieName() {
        return properties.authCookie().name();
    }

    private ResponseCookie buildCookie(String value, Duration maxAge) {
        return ResponseCookie.from(properties.authCookie().name(), value)
                .httpOnly(true)
                .secure(properties.authCookie().secure())
                .sameSite(properties.authCookie().sameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
