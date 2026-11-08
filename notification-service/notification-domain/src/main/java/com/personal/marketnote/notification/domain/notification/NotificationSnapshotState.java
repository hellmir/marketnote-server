package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSnapshotState {
    private final Long id;
    private final Long userId;
    private final NotificationType notificationType;
    private final String title;
    private final String body;
    private final String data;
    private final DeliveryChannel deliveryChannel;
    private final boolean isRead;
    private final String landingUrl;
    private final SendStatus sendStatus;
    private final LocalDateTime scheduledAt;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
