package com.personal.marketnote.notification.port.in.command;

public record RegisterNotificationTemplateCommand(
        String templateCode,
        String notificationType,
        String notificationCategory,
        String title,
        String bodyTemplate,
        String urlTemplate
) {
}
