package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.reward.domain.exception.InvalidContinuousPeriodException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContinuousPeriodTest {

    @Test
    @DisplayName("1로 생성 시 정상 생성된다")
    void shouldCreateWithOne() {
        ContinuousPeriod period = ContinuousPeriod.of((short) 1);

        assertThat(period.getValue()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("양수로 생성 시 정상 생성된다")
    void shouldCreateWithPositiveValue() {
        ContinuousPeriod period = ContinuousPeriod.of((short) 7);

        assertThat(period.getValue()).isEqualTo((short) 7);
    }

    @Test
    @DisplayName("0으로 생성 시 InvalidContinuousPeriodException이 발생한다")
    void shouldThrowWhenZero() {
        assertThatThrownBy(() -> ContinuousPeriod.of((short) 0))
                .isInstanceOf(InvalidContinuousPeriodException.class);
    }

    @Test
    @DisplayName("음수로 생성 시 InvalidContinuousPeriodException이 발생한다")
    void shouldThrowWhenNegative() {
        assertThatThrownBy(() -> ContinuousPeriod.of((short) -1))
                .isInstanceOf(InvalidContinuousPeriodException.class);
    }

    @Test
    @DisplayName("equals()와 hashCode()가 값 기반으로 동작한다")
    void shouldImplementEqualsAndHashCodeByValue() {
        ContinuousPeriod a = ContinuousPeriod.of((short) 3);
        ContinuousPeriod b = ContinuousPeriod.of((short) 3);
        ContinuousPeriod c = ContinuousPeriod.of((short) 5);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
