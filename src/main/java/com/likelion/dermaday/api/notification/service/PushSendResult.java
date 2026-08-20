package com.likelion.dermaday.api.notification.service;

import java.util.List;

public record PushSendResult(int acceptedCount, List<String> invalidFids) {
    public PushSendResult {
        if (acceptedCount < 0) {
            throw new IllegalArgumentException("Accepted count must not be negative");
        }
        invalidFids = List.copyOf(invalidFids);
    }
}
