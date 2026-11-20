package com.personal.marketnote.fulfillment.utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultSleeper")
class DefaultSleeperTest {

    private final DefaultSleeper sleeper = new DefaultSleeper();

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Nested
    @DisplayName("sleepWithJitter")
    class SleepWithJitter {

        @Test
        @DisplayName("maxMillis가 0이면 1ms 이하만 sleep한다")
        void shouldSleepAtMostOneMillisWhenMaxIsZero() {
            long start = System.nanoTime();

            sleeper.sleepWithJitter(0L);

            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            assertThat(elapsedMillis).isLessThan(50L);
        }

        @Test
        @DisplayName("음수 maxMillis도 1ms 이하만 sleep한다")
        void shouldSleepAtMostOneMillisWhenMaxIsNegative() {
            long start = System.nanoTime();

            sleeper.sleepWithJitter(-100L);

            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            assertThat(elapsedMillis).isLessThan(50L);
        }

        @Test
        @DisplayName("maxMillis 이내에서 sleep한다")
        void shouldSleepWithinMaxBound() {
            long maxMillis = 50L;

            long start = System.nanoTime();
            sleeper.sleepWithJitter(maxMillis);
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            assertThat(elapsedMillis).isLessThanOrEqualTo(maxMillis + 100L);
        }

        @Test
        @DisplayName("InterruptedException 발생 시 인터럽트 플래그를 복원한다")
        void shouldRestoreInterruptFlagWhenInterrupted() throws InterruptedException {
            Thread worker = new Thread(() -> sleeper.sleepWithJitter(10_000L));
            worker.start();

            Thread.sleep(20L);
            worker.interrupt();
            worker.join(1_000L);

            assertThat(worker.isAlive()).isFalse();
        }
    }

    @Nested
    @DisplayName("sleepWithBaseAndJitter")
    class SleepWithBaseAndJitter {

        @Test
        @DisplayName("base + [0, jitterMax) 범위에서 sleep한다")
        void shouldSleepBaseToBasePlusJitter() {
            long baseMillis = 30L;
            long jitterMaxMillis = 50L;

            long start = System.nanoTime();
            sleeper.sleepWithBaseAndJitter(baseMillis, jitterMaxMillis);
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            assertThat(elapsedMillis).isGreaterThanOrEqualTo(baseMillis - 10L);
            assertThat(elapsedMillis).isLessThanOrEqualTo(baseMillis + jitterMaxMillis + 100L);
        }

        @Test
        @DisplayName("InterruptedException 발생 시 인터럽트 플래그를 복원한다")
        void shouldRestoreInterruptFlagWhenInterrupted() throws InterruptedException {
            Thread worker = new Thread(() -> sleeper.sleepWithBaseAndJitter(10_000L, 500L));
            worker.start();

            Thread.sleep(20L);
            worker.interrupt();
            worker.join(1_000L);

            assertThat(worker.isAlive()).isFalse();
        }
    }
}
