package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.port.in.usecase.order.AutoConfirmDeliveredOrdersUseCase;
import com.personal.marketnote.common.scheduler.ShedLockNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "order.auto-confirm.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class AutoConfirmDeliveredOrdersScheduler {
    private final AutoConfirmDeliveredOrdersUseCase autoConfirmDeliveredOrdersUseCase;

    @Scheduled(cron = "${order.auto-confirm.scheduler.cron}", zone = "Asia/Seoul")
    @SchedulerLock(name = ShedLockNames.COMMERCE_AUTO_CONFIRM_DELIVERED_ORDERS, lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void autoConfirmDeliveredOrders() {
        log.info("구매 자동 확정 스케줄러 실행 시작");
        try {
            autoConfirmDeliveredOrdersUseCase.autoConfirmDeliveredOrders();
            log.info("구매 자동 확정 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("구매 자동 확정 스케줄러 실행 실패", e);
        }
    }
}
