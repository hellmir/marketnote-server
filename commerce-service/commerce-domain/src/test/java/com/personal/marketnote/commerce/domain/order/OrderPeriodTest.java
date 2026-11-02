package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderPeriod 테스트")
class OrderPeriodTest {

    @Test
    @DisplayName("ONE_MONTH는 현재 날짜에서 1개월 전 자정을 반환한다")
    void shouldReturnOneMonthAgoMidnight() {
        // given
        LocalDate now = LocalDate.of(2026, 4, 11);

        // when
        LocalDateTime startDate = OrderPeriod.ONE_MONTH.startDate(now);

        // then
        assertThat(startDate).isEqualTo(LocalDateTime.of(now.minusMonths(1), LocalTime.MIDNIGHT));
    }

    @Test
    @DisplayName("THREE_MONTHS는 현재 날짜에서 3개월 전 자정을 반환한다")
    void shouldReturnThreeMonthsAgoMidnight() {
        // given
        LocalDate now = LocalDate.of(2026, 4, 11);

        // when
        LocalDateTime startDate = OrderPeriod.THREE_MONTHS.startDate(now);

        // then
        assertThat(startDate).isEqualTo(LocalDateTime.of(now.minusMonths(3), LocalTime.MIDNIGHT));
    }

    @Test
    @DisplayName("SIX_MONTHS는 현재 날짜에서 6개월 전 자정을 반환한다")
    void shouldReturnSixMonthsAgoMidnight() {
        // given
        LocalDate now = LocalDate.of(2026, 4, 11);

        // when
        LocalDateTime startDate = OrderPeriod.SIX_MONTHS.startDate(now);

        // then
        assertThat(startDate).isEqualTo(LocalDateTime.of(now.minusMonths(6), LocalTime.MIDNIGHT));
    }

    @Test
    @DisplayName("ONE_YEAR는 현재 날짜에서 12개월 전 자정을 반환한다")
    void shouldReturnOneYearAgoMidnight() {
        // given
        LocalDate now = LocalDate.of(2026, 4, 11);

        // when
        LocalDateTime startDate = OrderPeriod.ONE_YEAR.startDate(now);

        // then
        assertThat(startDate).isEqualTo(LocalDateTime.of(now.minusMonths(12), LocalTime.MIDNIGHT));
    }

    @Test
    @DisplayName("ALL은 null을 반환한다")
    void shouldReturnNullForAll() {
        // given
        LocalDate now = LocalDate.of(2026, 4, 11);

        // when
        LocalDateTime startDate = OrderPeriod.ALL.startDate(now);

        // then
        assertThat(startDate).isNull();
    }
}
