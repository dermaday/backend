package com.likelion.dermaday.api.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "app.push")
public record PushProperties(
        boolean enabled,
        String projectId,
        ZoneId zoneId,
        LocalTime sendTime,
        URI homeUrl,
        Duration dispatchGrace,
        Duration pollInterval
) {
}
