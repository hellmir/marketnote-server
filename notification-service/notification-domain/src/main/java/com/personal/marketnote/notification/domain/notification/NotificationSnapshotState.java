package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationSnapshotState {
    private Long id;
    private Long userId;
    private NotificationType notificationType;
    private String title;
    private String body;
    private String data;
    private DeliveryChannel deliveryChannel;
    private boolean isRead;
    private String landingUrl;
    private SendStatus sendStatus;
    private LocalDateTime scheduledAt;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
