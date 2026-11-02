package com.personal.marketnote.product.domain.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.exception.InvalidRateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateTest {

    @Test
    @DisplayName("BigDecimal.ZERO로 생성 시 정상 생성된다")
    void createRateWithZero() {
        Rate rate = Rate.of(BigDecimal.ZERO);

        assertThat(rate.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("최대값 100으로 생성 시 정상 생성된다")
    void createRateWithMaxValue() {
        Rate rate = Rate.of(new BigDecimal("100.0"));

        assertThat(rate.getValue()).isEqualByComparingTo(new BigDecimal("100.0"));
    }

    @Test
    @DisplayName("0~100 범위 소수 한 자리 값으로 생성 시 정상 생성된다")
    void createRateWithFractionalValue() {
        Rate rate = Rate.of(new BigDecimal("50.5"));

        assertThat(rate.getValue()).isEqualByComparingTo(new BigDecimal("50.5"));
    }

    @Test
    @DisplayName("음수로 생성 시 InvalidRateException 발생한다")
    void throwsExceptionWhenRateIsNegative() {
        assertThatThrownBy(() -> Rate.of(new BigDecimal("-1.0")))
                .isInstanceOf(InvalidRateException.class);
    }

    @Test
    @DisplayName("100 초과 값으로 생성 시 InvalidRateException 발생한다")
    void throwsExceptionWhenRateExceedsHundred() {
        assertThatThrownBy(() -> Rate.of(new BigDecimal("100.1")))
                .isInstanceOf(InvalidRateException.class);
    }

    @Test
    @DisplayName("소수 두 자리 이상 값으로 생성 시 InvalidRateException 발생한다")
    void throwsExceptionWhenScaleExceedsOne() {
        assertThatThrownBy(() -> Rate.of(new BigDecimal("0.01")))
                .isInstanceOf(InvalidRateException.class);
    }

    @Test
    @DisplayName("null로 생성 시 InvalidRateException 발생한다")
    void throwsExceptionWhenRateIsNull() {
        assertThatThrownBy(() -> Rate.of(null))
                .isInstanceOf(InvalidRateException.class);
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환한다")
    void getValueReturnsOriginalValue() {
        BigDecimal input = new BigDecimal("15.0");

        Rate rate = Rate.of(input);

        assertThat(rate.getValue()).isEqualByComparingTo(input);
    }
}
