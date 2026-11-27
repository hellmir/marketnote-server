package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
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
