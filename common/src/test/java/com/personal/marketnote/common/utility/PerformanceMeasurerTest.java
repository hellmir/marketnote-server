package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceMeasurerTest {

    @Test
    @DisplayName("경과 시간은 시작 시각과 현재 시각의 차이를 반환한다")
    void shouldReturnElapsedTimeBetweenStartAndNow() {
        // given
        long startedAt = System.currentTimeMillis() - 100;

        // when
        long elapsed = PerformanceMeasurer.computeElapsedTime(startedAt);

        // then
        assertThat(elapsed).isGreaterThanOrEqualTo(100);
    }

    @Test
    @DisplayName("시작 시각이 0이면 현재 시각을 밀리초로 반환한다")
    void shouldReturnCurrentTimeWhenStartedAtIsZero() {
        // given
        long before = System.currentTimeMillis();

        // when
        long result = PerformanceMeasurer.computeElapsedTime(0);

        // then
        long after = System.currentTimeMillis();
        assertThat(result).isBetween(before, after);
    }

    @Test
    @DisplayName("사용 메모리는 beforeMemory를 차감한 값을 반환한다")
    void shouldReturnUsedMemoryMinusBeforeMemory() {
        // given
        long beforeMemory = 0;

        // when
        long usedMemory = PerformanceMeasurer.computeUsedMemory(beforeMemory);

        // then
        assertThat(usedMemory).isGreaterThan(0);
    }

    @Test
    @DisplayName("beforeMemory가 현재 사용량과 동일하면 0 근처 값을 반환한다")
    void shouldReturnNearZeroWhenBeforeMemoryEqualsCurrentUsage() {
        // given
        long currentUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // when
        long usedMemory = PerformanceMeasurer.computeUsedMemory(currentUsage);

        // then
        assertThat(Math.abs(usedMemory)).isLessThan(1_000_000);
    }
}
