package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.notification.PublishScheduledNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledNotificationPublisher {

    private final PublishScheduledNotificationsUseCase publishScheduledNotificationsUseCase;

    @Scheduled(fixedDelayString = "${notification.scheduler.scheduled-publish-interval-ms:60000}")
    public void publishScheduledNotifications() {
        log.debug("예약 알림 발송 스케줄러 실행");
        int processedCount = publishScheduledNotificationsUseCase.publishScheduledNotifications();
        if (processedCount > 0) {
            log.info("예약 알림 발송 스케줄러 완료: {}건 처리", processedCount);
        }
    }
}
