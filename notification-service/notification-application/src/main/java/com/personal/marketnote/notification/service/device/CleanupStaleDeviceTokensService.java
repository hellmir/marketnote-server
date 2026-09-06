package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.usecase.device.CleanupStaleDeviceTokensUseCase;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CleanupStaleDeviceTokensService implements CleanupStaleDeviceTokensUseCase {

    private static final int STALE_DAYS = 90;

    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final Clock clock;

    @Override
    public int cleanupStaleDeviceTokens() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(STALE_DAYS);
        int deactivatedCount = deleteDeviceTokenPort.deactivateStaleTokens(threshold);

        if (deactivatedCount > 0) {
            log.info("Stale 디바이스 토큰 정리 완료: {}건 비활성화", deactivatedCount);
        }

        return deactivatedCount;
    }
}
