package com.personal.marketnote.notification.port.in.command;

public record UpdateNotificationTemplateCommand(
        String title,
        String bodyTemplate,
        String urlTemplate
) {
}
