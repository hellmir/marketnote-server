package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.SettlementSchedulerProperties;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final ExecuteSettlementUseCase executeSettlementUseCase;
    private final SettlementSchedulerProperties properties;
    private final Clock commerceClock;

    @Scheduled(cron = "${settlement.scheduler.cron}", zone = "Asia/Seoul")
    public void executeMonthlySettlement() {
        YearMonth previousMonth = YearMonth.now(commerceClock).minusMonths(1);
        int year = previousMonth.getYear();
        int month = previousMonth.getMonthValue();

        log.info("월별 자동 정산 실행 시작 - year: {}, month: {}, pgFeeRate: {}, platformFeeRate: {}",
                year, month, properties.getPgFeeRate(), properties.getPlatformFeeRate());

        try {
            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(year)
                    .month(month)
                    .pgFeeRate(properties.getPgFeeRate())
                    .platformFeeRate(properties.getPlatformFeeRate())
                    .build();

            executeSettlementUseCase.executeSettlement(command);
            log.info("월별 자동 정산 실행 완료 - year: {}, month: {}", year, month);
        } catch (NoUnsettledAllocationException e) {
            log.info("전월 미정산 배분 없음, 정산 스킵 - year: {}, month: {}", year, month);
        } catch (Exception e) {
            log.error("월별 자동 정산 실행 실패 - year: {}, month: {}", year, month, e);
        }
    }
}
