package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationTemplateSnapshotState {
    private Long id;
    private String templateCode;
    private NotificationType notificationType;
    private NotificationCategory notificationCategory;
    private String title;
    private String bodyTemplate;
    private String urlTemplate;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
