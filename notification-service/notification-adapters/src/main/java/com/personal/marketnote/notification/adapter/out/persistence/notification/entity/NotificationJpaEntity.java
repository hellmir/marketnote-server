package com.personal.marketnote.notification.adapter.out.persistence.notification.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user_id_status", columnList = "user_id, status")
})
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationJpaEntity extends BaseGeneralEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    @Column(name = "delivery_channel", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private DeliveryChannel deliveryChannel;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    @Column(name = "send_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SendStatus sendStatus;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    public void updateFrom(Notification notification) {
        this.sendStatus = notification.getSendStatus();
        this.isRead = notification.isRead();
    }

    public static NotificationJpaEntity from(Notification notification) {
        return NotificationJpaEntity.builder()
                .userId(notification.getUserId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .data(notification.getData())
                .deliveryChannel(notification.getDeliveryChannel())
                .isRead(notification.isRead())
                .landingUrl(notification.getLandingUrl())
                .sendStatus(notification.getSendStatus())
                .scheduledAt(notification.getScheduledAt())
                .build();
    }
}
