package com.personal.marketnote.user.adapter.in.scheduler;

import com.personal.marketnote.user.port.in.result.ActivateExpiredDeactivationResult;
import com.personal.marketnote.user.port.in.usecase.user.ActivateExpiredDeactivationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "user.activate-expired-deactivation.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ActivateExpiredDeactivationScheduler {
    private final ActivateExpiredDeactivationUseCase activateExpiredDeactivationUseCase;

    @Scheduled(cron = "${user.activate-expired-deactivation.scheduler.cron}", zone = "Asia/Seoul")
    public void activateExpiredDeactivations() {
        log.info("비활성화 기간 만료 자동 활성화 스케줄러 실행 시작");
        try {
            ActivateExpiredDeactivationResult result =
                    activateExpiredDeactivationUseCase.activateExpiredDeactivations();
            log.info("비활성화 기간 만료 자동 활성화 스케줄러 실행 완료 activatedCount={}", result.activatedCount());
        } catch (Exception e) {
            log.error("비활성화 기간 만료 자동 활성화 스케줄러 실행 실패", e);
        }
    }
}
