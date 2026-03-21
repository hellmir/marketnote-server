package com.personal.marketnote.product.service.popularity;

import com.personal.marketnote.product.port.out.pricepolicy.UpdatePopularityPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePopularityUseCaseTest {
    @InjectMocks
    private UpdatePopularityService updatePopularityService;

    @Mock
    private UpdatePopularityPort updatePopularityPort;

    @Mock(name = "productClock")
    private Clock productClock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(productClock.instant()).thenReturn(fixedClock.instant());
        when(productClock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("주간 인기도 갱신 시 현재 시간 기준 7일 전 시간으로 Port를 호출한다")
    void updateWeeklyPopularity_callsPortWithSevenDaysAgo() {
        // given
        setUpClock("2026-03-21T06:00:00Z");
        LocalDateTime expectedSince = LocalDateTime.of(2026, 3, 5, 15, 0, 0);

        // when
        updatePopularityService.updateWeeklyPopularity();

        // then
        verify(updatePopularityPort).updateWeeklyPopularity(expectedSince);
    }

    @Test
    @DisplayName("연도가 바뀌는 시점에도 7일 전 시간이 올바르게 계산된다")
    void updateWeeklyPopularity_yearBoundary_calculatesCorrectly() {
        // given
        setUpClock("2026-01-03T00:00:00Z");
        LocalDateTime expectedSince = LocalDateTime.of(2025, 12, 27, 9, 0, 0);

        // when
        updatePopularityService.updateWeeklyPopularity();

        // then
        verify(updatePopularityPort).updateWeeklyPopularity(expectedSince);
    }

    @Test
    @DisplayName("주간 인기도 갱신 시 Port가 정확히 한 번 호출된다")
    void updateWeeklyPopularity_callsPortOnce() {
        // given
        setUpClock("2026-06-15T12:00:00Z");

        // when
        updatePopularityService.updateWeeklyPopularity();

        // then
        verify(updatePopularityPort).updateWeeklyPopularity(LocalDateTime.of(2026, 6, 8, 21, 0, 0));
    }
}
