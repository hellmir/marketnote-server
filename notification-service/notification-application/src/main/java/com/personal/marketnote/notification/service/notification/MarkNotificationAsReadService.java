package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationNotFoundException;
import com.personal.marketnote.notification.port.in.command.MarkNotificationAsReadCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.MarkNotificationAsReadUseCase;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.sse.payload.UnreadCountChangedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class MarkNotificationAsReadService implements MarkNotificationAsReadUseCase {

    private final FindNotificationPort findNotificationPort;
    private final UpdateNotificationPort updateNotificationPort;
    private final PublishSseEventPort publishSseEventPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void markAsRead(MarkNotificationAsReadCommand command) {
        Notification notification = findNotificationPort.findActiveById(command.notificationId())
                .orElseThrow(() -> {
                    log.warn("알림을 찾을 수 없습니다. notificationId={}", command.notificationId());
                    return new NotificationNotFoundException();
                });

        if (!notification.isOwnedBy(command.userId())) {
            log.warn(
                    "알림 소유자 불일치. notificationId={}, userId={}",
                    command.notificationId(), command.userId()
            );
            throw new NotificationNotFoundException();
        }

        if (notification.isRead()) {
            return;
        }

        notification.markAsRead();
        updateNotificationPort.update(notification);

        long unreadCount = findNotificationPort.countUnreadByUserId(command.userId());
        publishSseEventPort.publish(
                command.userId(),
                NotificationSseEventType.UNREAD_COUNT_CHANGED,
                UnreadCountChangedPayload.of(unreadCount)
        );
    }
}
