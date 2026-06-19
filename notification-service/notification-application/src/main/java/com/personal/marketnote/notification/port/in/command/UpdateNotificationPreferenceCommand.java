package com.personal.marketnote.notification.port.in.command;

public record UpdateNotificationPreferenceCommand(
        Long userId,
        String notificationType,
        boolean enabled
) {
}
