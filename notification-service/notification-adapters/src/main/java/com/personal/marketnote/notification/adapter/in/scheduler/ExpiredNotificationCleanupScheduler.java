package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.common.scheduler.ShedLockNames;
import com.personal.marketnote.notification.port.in.usecase.notification.CleanupExpiredNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExpiredNotificationCleanupScheduler {

    private final CleanupExpiredNotificationsUseCase cleanupExpiredNotificationsUseCase;

    @Scheduled(cron = "${notification.cleanup.expired-notification-cron:0 0 4 * * *}")
    @SchedulerLock(name = ShedLockNames.NOTIFICATION_EXPIRED_CLEANUP, lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void cleanupExpiredNotifications() {
        log.debug("만료 알림 정리 스케줄러 실행");
        int deletedCount = cleanupExpiredNotificationsUseCase.cleanupExpiredNotifications();
        if (deletedCount > 0) {
            log.info("만료 알림 정리 스케줄러 완료: {}건 비활성화", deletedCount);
        }
    }
}
