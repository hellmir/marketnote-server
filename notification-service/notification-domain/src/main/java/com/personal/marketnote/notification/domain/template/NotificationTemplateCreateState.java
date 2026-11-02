package com.personal.marketnote.notification.domain.template;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationTemplateCreateState {
    private String templateCode;
    private NotificationType notificationType;
    private NotificationCategory notificationCategory;
    private String title;
    private String bodyTemplate;
    private String urlTemplate;
}
