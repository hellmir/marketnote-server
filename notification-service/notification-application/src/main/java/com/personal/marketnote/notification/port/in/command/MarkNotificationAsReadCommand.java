package com.personal.marketnote.notification.port.in.command;

public record MarkNotificationAsReadCommand(
        Long notificationId,
        Long userId
) {
}
