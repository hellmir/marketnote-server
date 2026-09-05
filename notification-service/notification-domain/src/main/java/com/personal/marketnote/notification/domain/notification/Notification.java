package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Notification extends BaseDomain {
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
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Notification from(NotificationCreateState state) {
        validate(state);

        SendStatus initialStatus = FormatValidator.hasValue(state.getScheduledAt())
                ? SendStatus.SCHEDULED
                : SendStatus.PENDING;

        Notification notification = Notification.builder()
                .userId(state.getUserId())
                .notificationType(state.getNotificationType())
                .title(state.getTitle())
                .body(state.getBody())
                .data(state.getData())
                .deliveryChannel(state.getDeliveryChannel())
                .isRead(false)
                .landingUrl(state.getLandingUrl())
                .sendStatus(initialStatus)
                .scheduledAt(state.getScheduledAt())
                .build();
        notification.activate();
        return notification;
    }

    public static Notification from(NotificationSnapshotState state) {
        Notification notification = Notification.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .notificationType(state.getNotificationType())
                .title(state.getTitle())
                .body(state.getBody())
                .data(state.getData())
                .deliveryChannel(state.getDeliveryChannel())
                .isRead(state.isRead())
                .landingUrl(state.getLandingUrl())
                .sendStatus(state.getSendStatus())
                .scheduledAt(state.getScheduledAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        notification.status = state.getStatus();
        return notification;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsSent() {
        this.sendStatus = SendStatus.SENT;
    }

    public void markAsFailed() {
        this.sendStatus = SendStatus.FAILED;
    }

    public void markAsSkipped() {
        this.sendStatus = SendStatus.SKIPPED;
    }

    public void markAsPending() {
        if (!this.sendStatus.isScheduled()) {
            throw new InvalidNotificationException(
                    "SCHEDULED 상태에서만 PENDING으로 전환할 수 있습니다. 현재 상태: " + this.sendStatus);
        }
        this.sendStatus = SendStatus.PENDING;
    }

    private static void validate(NotificationCreateState state) {
        if (FormatValidator.hasNoValue(state.getUserId())) {
            throw new InvalidNotificationException("사용자 ID는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getNotificationType())) {
            throw new InvalidNotificationException("알림 타입은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getDeliveryChannel())) {
            throw new InvalidNotificationException("발송 채널은 필수입니다.");
        }
    }
}
