package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.InventoryReservationSchedulerProperties;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ExpireInventoryReservationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "inventory.reservation.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class InventoryReservationTimeoutChecker {
    private final ExpireInventoryReservationUseCase expireInventoryReservationUseCase;
    private final InventoryReservationSchedulerProperties properties;
    private final Clock commerceClock;

    @Scheduled(fixedDelayString = "${inventory.reservation.scheduler.check-interval-ms:60000}")
    public void checkExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now(commerceClock)
                .minusMinutes(properties.getTimeoutMinutes());
        expireInventoryReservationUseCase.expireTimedOutReservations(cutoff);
    }
}
