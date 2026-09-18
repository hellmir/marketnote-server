package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import com.personal.marketnote.reward.domain.exception.PointAmountNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointAmountTest {

    @Test
    @DisplayName("양의 정수 문자열로 PointAmount를 생성한다")
    void shouldCreatePointAmountWithPositiveInteger() {
        PointAmount pointAmount = PointAmount.of("100");

        assertThat(pointAmount.getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("0으로 PointAmount를 생성한다")
    void shouldCreatePointAmountWithZero() {
        PointAmount pointAmount = PointAmount.of("0");

        assertThat(pointAmount.getValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("null이면 PointAmountNoValueException을 던진다")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThatThrownBy(() -> PointAmount.of(null))
                .isInstanceOf(PointAmountNoValueException.class);
    }

    @Test
    @DisplayName("빈 문자열이면 PointAmountNoValueException을 던진다")
    void shouldThrowExceptionWhenAmountIsBlank() {
        assertThatThrownBy(() -> PointAmount.of(""))
                .isInstanceOf(PointAmountNoValueException.class);
    }

    @Test
    @DisplayName("음수이면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThatThrownBy(() -> PointAmount.of("-1"))
                .isInstanceOf(InvalidPointAmountException.class);
    }

    @Test
    @DisplayName("비정수 문자열이면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenAmountIsNotInteger() {
        assertThatThrownBy(() -> PointAmount.of("abc"))
                .isInstanceOf(InvalidPointAmountException.class);
    }

    @Test
    @DisplayName("소수이면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenAmountIsDecimal() {
        assertThatThrownBy(() -> PointAmount.of("1.5"))
                .isInstanceOf(InvalidPointAmountException.class);
    }

    @Test
    @DisplayName("적립 시 현재 포인트에 요청 금액을 더한다")
    void shouldAccumulateWhenIsAccrual() {
        PointAmount current = PointAmount.of("100");

        PointAmount result = PointAmount.generateChangedAmount(true, current, 50L);

        assertThat(result.getValue()).isEqualTo(150L);
    }

    @Test
    @DisplayName("차감 시 현재 포인트에서 요청 금액을 뺀다")
    void shouldReduceWhenIsNotAccrual() {
        PointAmount current = PointAmount.of("100");

        PointAmount result = PointAmount.generateChangedAmount(false, current, 30L);

        assertThat(result.getValue()).isEqualTo(70L);
    }

    @Test
    @DisplayName("차감 결과가 음수이면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenReduceResultIsNegative() {
        PointAmount current = PointAmount.of("30");

        assertThatThrownBy(() -> PointAmount.generateChangedAmount(false, current, 50L))
                .isInstanceOf(InvalidPointAmountException.class);
    }
}
