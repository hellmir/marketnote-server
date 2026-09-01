package com.personal.marketnote.notification.adapter.out.mapper;

import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationSnapshotState;

import java.util.Optional;

public class NotificationJpaEntityToDomainMapper {

    private NotificationJpaEntityToDomainMapper() {
    }

    public static Optional<Notification> mapToDomain(NotificationJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> Notification.from(
                        NotificationSnapshotState.builder()
                                .id(e.getId())
                                .userId(e.getUserId())
                                .notificationType(e.getNotificationType())
                                .title(e.getTitle())
                                .body(e.getBody())
                                .data(e.getData())
                                .deliveryChannel(e.getDeliveryChannel())
                                .isRead(e.isRead())
                                .landingUrl(e.getLandingUrl())
                                .sendStatus(e.getSendStatus())
                                .scheduledAt(e.getScheduledAt())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
