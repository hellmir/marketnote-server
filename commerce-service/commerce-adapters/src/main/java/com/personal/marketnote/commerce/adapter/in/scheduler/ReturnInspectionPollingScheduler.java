package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.port.in.usecase.returntracker.PollReturnInspectionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "return-inspection.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class ReturnInspectionPollingScheduler {
    private final PollReturnInspectionUseCase pollReturnInspectionUseCase;

    @Scheduled(fixedDelayString = "${return-inspection.scheduler.fixed-delay-ms:1800000}")
    public void pollReturnInspections() {
        log.info("반품 검수 폴링 시작");
        try {
            pollReturnInspectionUseCase.pollPendingInspections();
            log.info("반품 검수 폴링 완료");
        } catch (Exception e) {
            log.error("반품 검수 폴링 실패: {}", e.getMessage(), e);
        }
    }
}
