package com.personal.marketnote.notification.port.in.result.template;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationType;

import java.time.LocalDateTime;

public record GetNotificationTemplateResult(
        Long id,
        String templateCode,
        NotificationType notificationType,
        NotificationCategory notificationCategory,
        String title,
        String bodyTemplate,
        String urlTemplate,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static GetNotificationTemplateResult from(NotificationTemplate template) {
        return new GetNotificationTemplateResult(
                template.getId(),
                template.getTemplateCode(),
                template.getNotificationType(),
                template.getNotificationCategory(),
                template.getTitle(),
                template.getBodyTemplate(),
                template.getUrlTemplate(),
                template.getCreatedAt(),
                template.getModifiedAt()
        );
    }
}
