package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.device.CleanupStaleDeviceTokensUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StaleDeviceTokenCleanupScheduler {

    private final CleanupStaleDeviceTokensUseCase cleanupStaleDeviceTokensUseCase;

    @Scheduled(cron = "${notification.cleanup.stale-token-cron:0 0 4 * * *}")
    public void cleanupStaleDeviceTokens() {
        log.debug("Stale 디바이스 토큰 정리 스케줄러 실행");
        int deactivatedCount = cleanupStaleDeviceTokensUseCase.cleanupStaleDeviceTokens();
        if (deactivatedCount > 0) {
            log.info("Stale 디바이스 토큰 정리 스케줄러 완료: {}건 비활성화", deactivatedCount);
        }
    }
}
