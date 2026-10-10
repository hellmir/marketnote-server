package com.personal.marketnote.notification.domain.vendorcommunication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationVendorName {
    FCM("Firebase Cloud Messaging");

    private final String description;
}
