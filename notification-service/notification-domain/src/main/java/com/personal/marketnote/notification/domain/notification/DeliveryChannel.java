package com.personal.marketnote.notification.domain.notification;

public enum DeliveryChannel {
    PUSH_ONLY("푸시"),
    IN_APP_ONLY("인앱"),
    PUSH_AND_IN_APP("푸시+인앱");

    private final String description;

    DeliveryChannel(String description) {
        this.description = description;
    }

    public boolean hasPush() {
        return this == PUSH_ONLY || this == PUSH_AND_IN_APP;
    }

    public boolean hasInApp() {
        return this == IN_APP_ONLY || this == PUSH_AND_IN_APP;
    }

    public String getDescription() {
        return description;
    }
}
