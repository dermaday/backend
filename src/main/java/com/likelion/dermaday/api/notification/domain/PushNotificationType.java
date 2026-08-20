package com.likelion.dermaday.api.notification.domain;

public enum PushNotificationType {
    PRODUCT_UNLOCK(10),
    ALL_UNLOCK_TOMORROW(20),
    ALL_UNLOCKED(30);

    private final int priority;

    PushNotificationType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
