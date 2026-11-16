package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.InventoryReservationSchedulerProperties;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ExpireInventoryReservationUseCase;
import com.personal.marketnote.common.scheduler.ShedLockNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    @SchedulerLock(name = ShedLockNames.COMMERCE_INVENTORY_RESERVATION_TIMEOUT, lockAtMostFor = "PT5M", lockAtLeastFor = "PT5S")
    public void checkExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now(commerceClock)
                .minusMinutes(properties.getTimeoutMinutes());
        expireInventoryReservationUseCase.expireTimedOutReservations(cutoff);
    }
}
