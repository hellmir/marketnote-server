package com.personal.marketnote.fulfillment.adapter.in.scheduler;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFulfillmentAllStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentStockSyncScheduler {
    private static final String SYNC_CRON = "0 0 0,7 * * *";
    private static final String SYNC_ZONE = "Asia/Seoul";

    private final SyncFulfillmentAllStockUseCase syncFulfillmentAllStockUseCase;
    private final FulfillmentAuthProperties fasstoAuthProperties;

    /**
     * (관리자) 일일 파스토 전체 재고 동기화
     *
     * @Author 성효빈
     * @Date 2026-02-08
     * @Description 매일 00시, 07시에 파스토 전체 재고를 동기화합니다.
     */
    @Scheduled(cron = SYNC_CRON, zone = SYNC_ZONE)
    public void syncAllStocks() {
        String customerCode = fasstoAuthProperties.getCustomerCode();
        if (FormatValidator.hasNoValue(customerCode)) {
            log.warn("Fulfillment customer code is missing. Scheduled stock sync is skipped.");
            return;
        }

        try {
            syncFulfillmentAllStockUseCase.syncAll(SyncFulfillmentAllStockCommand.of(customerCode));
        } catch (Exception e) {
            log.error("Failed to sync Fulfillment stocks: customerCode={}", customerCode, e);
        }
    }
}
