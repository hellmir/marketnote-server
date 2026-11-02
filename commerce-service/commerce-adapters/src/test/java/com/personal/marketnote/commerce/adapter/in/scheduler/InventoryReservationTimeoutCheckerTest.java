package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.InventoryReservationSchedulerProperties;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ExpireInventoryReservationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationTimeoutCheckerTest {
    @Mock
    private ExpireInventoryReservationUseCase expireInventoryReservationUseCase;
    @Mock
    private InventoryReservationSchedulerProperties properties;
    @Mock(name = "commerceClock")
    private Clock commerceClock;

    @InjectMocks
    private InventoryReservationTimeoutChecker inventoryReservationTimeoutChecker;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(commerceClock.instant()).thenReturn(fixedClock.instant());
        when(commerceClock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("cutoff 시각이 현재 시각 - timeoutMinutes로 계산되어 UseCase에 전달된다")
    void checkExpiredReservations_cutoffCalculatedCorrectly() {
        // given
        setUpClock("2026-04-05T00:00:00Z");
        when(properties.getTimeoutMinutes()).thenReturn(10L);

        // when
        inventoryReservationTimeoutChecker.checkExpiredReservations();

        // then
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(expireInventoryReservationUseCase).expireTimedOutReservations(cutoffCaptor.capture());

        LocalDateTime expectedCutoff = LocalDateTime.of(2026, 4, 2, 8, 50, 0);
        assertThat(cutoffCaptor.getValue()).isEqualTo(expectedCutoff);
    }

    @Test
    @DisplayName("UseCase를 정확히 한 번 호출한다")
    void checkExpiredReservations_callsUseCaseOnce() {
        // given
        setUpClock("2026-04-05T00:00:00Z");
        when(properties.getTimeoutMinutes()).thenReturn(10L);

        // when
        inventoryReservationTimeoutChecker.checkExpiredReservations();

        // then
        verify(expireInventoryReservationUseCase, times(1)).expireTimedOutReservations(any(LocalDateTime.class));
        verifyNoMoreInteractions(expireInventoryReservationUseCase);
    }

    @Test
    @DisplayName("timeoutMinutes 설정값에 따라 cutoff가 달라진다")
    void checkExpiredReservations_respectsTimeoutMinutesProperty() {
        // given
        setUpClock("2026-04-05T00:00:00Z");
        when(properties.getTimeoutMinutes()).thenReturn(30L);

        // when
        inventoryReservationTimeoutChecker.checkExpiredReservations();

        // then
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(expireInventoryReservationUseCase).expireTimedOutReservations(cutoffCaptor.capture());

        LocalDateTime expectedCutoff = LocalDateTime.of(2026, 4, 2, 8, 30, 0);
        assertThat(cutoffCaptor.getValue()).isEqualTo(expectedCutoff);
    }
}
