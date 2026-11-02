package com.personal.marketnote.notification.port.in.result.notification;

public record SendBatchNotificationResult(
        int totalUserCount,
        int sentUserCount,
        int skippedUserCount,
        int failedUserCount,
        int totalDeviceSentCount,
        int totalDeviceFailedCount
) {
}
