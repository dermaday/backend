package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.notification.domain.PushNotificationType;

import java.net.URI;
import java.util.Objects;

public record PushMessage(
        String title,
        String body,
        URI link,
        PushNotificationType type
) {
    public PushMessage {
        Objects.requireNonNull(title, "Title must not be null");
        Objects.requireNonNull(body, "Body must not be null");
        Objects.requireNonNull(link, "Link must not be null");
        Objects.requireNonNull(type, "Type must not be null");
    }
}
