package com.personal.marketnote.notification.port.in.result.template;

import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationType;

public record UpdateNotificationTemplateResult(
        Long id,
        String templateCode,
        NotificationType notificationType,
        String title,
        String bodyTemplate,
        String urlTemplate
) {

    public static UpdateNotificationTemplateResult from(NotificationTemplate template) {
        return new UpdateNotificationTemplateResult(
                template.getId(),
                template.getTemplateCode(),
                template.getNotificationType(),
                template.getTitle(),
                template.getBodyTemplate(),
                template.getUrlTemplate()
        );
    }
}
