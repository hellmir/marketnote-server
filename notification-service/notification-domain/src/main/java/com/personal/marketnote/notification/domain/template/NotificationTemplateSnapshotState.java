package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationTemplateSnapshotState {
    private final Long id;
    private final String templateCode;
    private final NotificationType notificationType;
    private final NotificationCategory notificationCategory;
    private final String title;
    private final String bodyTemplate;
    private final String urlTemplate;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
