package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.AutoConfirmSchedulerProperties;
import com.personal.marketnote.commerce.port.in.usecase.order.AutoConfirmDeliveredOrdersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "order.auto-confirm.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class AutoConfirmDeliveredOrdersScheduler {
    private final AutoConfirmDeliveredOrdersUseCase autoConfirmDeliveredOrdersUseCase;
    private final AutoConfirmSchedulerProperties properties;

    @Scheduled(cron = "${order.auto-confirm.scheduler.cron}", zone = "Asia/Seoul")
    public void autoConfirmDeliveredOrders() {
        log.info("구매 자동 확정 스케줄러 실행 시작");
        try {
            autoConfirmDeliveredOrdersUseCase.autoConfirmDeliveredOrders(properties.getAutoConfirmDays());
            log.info("구매 자동 확정 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("구매 자동 확정 스케줄러 실행 실패", e);
        }
    }
}
