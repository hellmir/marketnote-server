package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.NotificationCreateState;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;

import java.time.LocalDateTime;

public class NotificationCommandToStateMapper {

    private NotificationCommandToStateMapper() {
    }

    public static NotificationCreateState mapToState(
            SendNotificationCommand command, NotificationTemplate template,
            String title, String body, String landingUrl,
            DeliveryChannel deliveryChannel, LocalDateTime scheduledAt
    ) {
        return NotificationCreateState.builder()
                .userId(command.userId())
                .notificationType(template.getNotificationType())
                .title(title)
                .body(body)
                .deliveryChannel(deliveryChannel)
                .landingUrl(landingUrl)
                .scheduledAt(scheduledAt)
                .build();
    }

    public static NotificationCreateState mapToSkippedState(
            SendNotificationCommand command, NotificationTemplate template, DeliveryChannel deliveryChannel
    ) {
        return NotificationCreateState.builder()
                .userId(command.userId())
                .notificationType(template.getNotificationType())
                .title(template.getTitle())
                .body(template.getBodyTemplate())
                .deliveryChannel(deliveryChannel)
                .build();
    }

    public static NotificationCreateState mapToBatchState(
            Long userId, NotificationTemplate template,
            String title, String body, String landingUrl,
            DeliveryChannel deliveryChannel, LocalDateTime scheduledAt
    ) {
        return NotificationCreateState.builder()
                .userId(userId)
                .notificationType(template.getNotificationType())
                .title(title)
                .body(body)
                .deliveryChannel(deliveryChannel)
                .landingUrl(landingUrl)
                .scheduledAt(scheduledAt)
                .build();
    }

    public static NotificationCreateState mapToBatchSkippedState(
            Long userId, NotificationTemplate template, DeliveryChannel deliveryChannel
    ) {
        return NotificationCreateState.builder()
                .userId(userId)
                .notificationType(template.getNotificationType())
                .title(template.getTitle())
                .body(template.getBodyTemplate())
                .deliveryChannel(deliveryChannel)
                .build();
    }
}
