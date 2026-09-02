package com.personal.marketnote.notification.port.in.result.notification;

public record SendNotificationResult(
        Long notificationId,
        String sendStatus,
        int sentDeviceCount,
        int failedDeviceCount
) {
}
