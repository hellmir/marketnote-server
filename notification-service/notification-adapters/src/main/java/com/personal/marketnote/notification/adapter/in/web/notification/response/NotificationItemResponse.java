package com.personal.marketnote.notification.adapter.in.web.notification.response;

import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.port.in.result.notification.NotificationItemResult;

import java.time.LocalDateTime;

public record NotificationItemResponse(
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
    public static NotificationItemResponse from(NotificationItemResult result) {
        return new NotificationItemResponse(
                result.id(),
                result.notificationType(),
                result.notificationTypeDescription(),
                result.title(),
                result.body(),
                result.deliveryChannel(),
                result.isRead(),
                result.landingUrl(),
                result.sendStatus(),
                result.scheduledAt(),
                result.createdAt()
        );
    }
}
