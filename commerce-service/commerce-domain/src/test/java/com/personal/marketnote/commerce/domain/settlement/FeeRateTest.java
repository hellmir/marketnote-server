package com.personal.marketnote.commerce.domain.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class FeeRateTest {

    @Test
    @DisplayName("0으로 FeeRate를 생성한다")
    void shouldCreateFeeRateWithZero() {
        FeeRate feeRate = FeeRate.of(0);

        assertThat(feeRate.getValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("10000(100%)으로 FeeRate를 생성한다")
    void shouldCreateFeeRateWithMaxValue() {
        FeeRate feeRate = FeeRate.of(10000);

        assertThat(feeRate.getValue()).isEqualTo(10000);
    }

    @Test
    @DisplayName("5000(50%)으로 FeeRate를 생성한다")
    void shouldCreateFeeRateWithMidValue() {
        FeeRate feeRate = FeeRate.of(5000);

        assertThat(feeRate.getValue()).isEqualTo(5000);
    }

    @Test
    @DisplayName("음수로 FeeRate 생성 시 InvalidFeeRateException을 던진다")
    void shouldThrowWhenFeeRateIsNegative() {
        assertThatThrownBy(() -> FeeRate.of(-1))
                .isInstanceOf(InvalidFeeRateException.class);
    }

    @Test
    @DisplayName("10001로 FeeRate 생성 시 InvalidFeeRateException을 던진다")
    void shouldThrowWhenFeeRateExceedsMaximum() {
        assertThatThrownBy(() -> FeeRate.of(10001))
                .isInstanceOf(InvalidFeeRateException.class);
    }

    @Test
    @DisplayName("toPercentage는 basis points를 퍼센트로 변환한다 (0 -> 0%)")
    void shouldConvertZeroBasisPointsToPercentage() {
        FeeRate feeRate = FeeRate.of(0);

        assertThat(feeRate.toPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("toPercentage는 basis points를 퍼센트로 변환한다 (10000 -> 100%)")
    void shouldConvertMaxBasisPointsToPercentage() {
        FeeRate feeRate = FeeRate.of(10000);

        assertThat(feeRate.toPercentage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("toPercentage는 basis points를 퍼센트로 변환한다 (250 -> 2.5%)")
    void shouldConvertPartialBasisPointsToPercentage() {
        FeeRate feeRate = FeeRate.of(250);

        assertThat(feeRate.toPercentage()).isEqualTo(2.5, within(0.0001));
    }

    @Test
    @DisplayName("같은 값의 FeeRate는 equals가 true다")
    void shouldBeEqualWhenSameValue() {
        FeeRate a = FeeRate.of(5000);
        FeeRate b = FeeRate.of(5000);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
