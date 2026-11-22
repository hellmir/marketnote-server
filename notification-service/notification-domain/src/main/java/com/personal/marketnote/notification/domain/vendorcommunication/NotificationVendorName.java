package com.personal.marketnote.notification.domain.vendorcommunication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationVendorName {
    FCM("Firebase Cloud Messaging"),
    ALIGO("Aligo 알림톡/친구톡");

    private final String description;
}
