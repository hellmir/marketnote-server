package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.CleanupExpiredNotificationsUseCase;
import com.personal.marketnote.notification.port.out.notification.DeleteNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CleanupExpiredNotificationsService implements CleanupExpiredNotificationsUseCase {

    private static final int EXPIRED_DAYS = 90;

    private final DeleteNotificationPort deleteNotificationPort;
    private final Clock clock;

    @Override
    public int cleanupExpiredNotifications() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(EXPIRED_DAYS);
        int deletedCount = deleteNotificationPort.deactivateExpiredNotifications(threshold);

        if (deletedCount > 0) {
            log.info("만료 알림 정리 완료: {}건 비활성화", deletedCount);
        }

        return deletedCount;
    }
}
