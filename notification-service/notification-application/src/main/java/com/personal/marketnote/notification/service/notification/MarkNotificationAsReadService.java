package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationNotFoundException;
import com.personal.marketnote.notification.port.in.command.MarkNotificationAsReadCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.MarkNotificationAsReadUseCase;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

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
                .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));

        if (!notification.isOwnedBy(command.userId())) {
            throw new NotificationNotFoundException(command.notificationId());
        }

        if (notification.isRead()) {
            return;
        }

        notification.markAsRead();
        updateNotificationPort.update(notification);

        long unreadCount = findNotificationPort.countUnreadByUserId(command.userId());
        publishSseEventPort.publish(command.userId(), "UNREAD_COUNT_CHANGED",
                "{\"unreadCount\":" + unreadCount + "}");
    }
}
