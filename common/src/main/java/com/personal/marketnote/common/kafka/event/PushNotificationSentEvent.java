package com.personal.marketnote.common.kafka.event;

import java.time.LocalDateTime;

public record PushNotificationSentEvent(
        Long notificationId,
        Long userId,
        String notificationType,
        String sendStatus,
        int sentDeviceCount,
        int failedDeviceCount,
        LocalDateTime occurredAt
) {
}
