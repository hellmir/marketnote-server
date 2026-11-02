package com.personal.marketnote.notification.port.in.result.template;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationType;

public record UpdateNotificationTemplateResult(
        Long id,
        String templateCode,
        NotificationType notificationType,
        NotificationCategory notificationCategory,
        String title,
        String bodyTemplate,
        String urlTemplate
) {

    public static UpdateNotificationTemplateResult from(NotificationTemplate template) {
        return new UpdateNotificationTemplateResult(
                template.getId(),
                template.getTemplateCode(),
                template.getNotificationType(),
                template.getNotificationCategory(),
                template.getTitle(),
                template.getBodyTemplate(),
                template.getUrlTemplate()
        );
    }
}
