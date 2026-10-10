package com.personal.marketnote.notification.domain.vendorcommunication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationVendorCommunicationTargetType {
    PUSH_NOTIFICATION("푸시 알림");

    private final String description;
}
