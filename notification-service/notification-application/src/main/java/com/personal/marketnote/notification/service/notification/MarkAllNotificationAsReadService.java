package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.MarkAllNotificationAsReadUseCase;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class MarkAllNotificationAsReadService implements MarkAllNotificationAsReadUseCase {

    private final UpdateNotificationPort updateNotificationPort;
    private final PublishSseEventPort publishSseEventPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void markAllAsRead(Long userId) {
        int updated = updateNotificationPort.markAllAsReadByUserId(userId);
        if (updated == 0) {
            return;
        }
        publishSseEventPort.publish(userId, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":0}");
    }
}
