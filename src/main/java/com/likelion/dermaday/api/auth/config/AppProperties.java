package com.likelion.dermaday.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendBaseUrl,
        String backendBaseUrl,
        Cors cors,
        Jwt jwt,
        AuthCookie authCookie
) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Jwt(String secret, Duration accessTokenTtl) {
    }

    public record AuthCookie(String name, boolean secure, String sameSite) {
    }
}
