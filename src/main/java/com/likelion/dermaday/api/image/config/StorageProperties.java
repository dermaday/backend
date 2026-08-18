package com.likelion.dermaday.api.image.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        String region,
        String bucket,
        Duration presignedUrlTtl
) {
}
