package com.personal.marketnote.notification.port.in.result.notification;

import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.SendStatus;

import java.time.LocalDateTime;

public record NotificationItemResult(
        Long id,
        String notificationType,
        String notificationTypeDescription,
        String title,
        String body,
        DeliveryChannel deliveryChannel,
        boolean isRead,
        String landingUrl,
        SendStatus sendStatus,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt
) {
    public static NotificationItemResult from(Notification notification) {
        return new NotificationItemResult(
                notification.getId(),
                notification.getNotificationType().name(),
                notification.getNotificationType().getDescription(),
                notification.getTitle(),
                notification.getBody(),
                notification.getDeliveryChannel(),
                notification.isRead(),
                notification.getLandingUrl(),
                notification.getSendStatus(),
                notification.getScheduledAt(),
                notification.getCreatedAt()
        );
    }
}
