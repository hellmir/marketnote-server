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
    @DisplayName("of(long)이 of(String)과 같은 값으로 인스턴스를 생성한다")
    void shouldCreatePointAmountWithLongEqualToStringFactory() {
        PointAmount fromLong = PointAmount.of(100L);
        PointAmount fromString = PointAmount.of("100");

        assertThat(fromLong.getValue()).isEqualTo(fromString.getValue());
    }

    @Test
    @DisplayName("of(long)에 음수를 전달하면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenOfLongIsNegative() {
        assertThatThrownBy(() -> PointAmount.of(-1L))
                .isInstanceOf(InvalidPointAmountException.class);
    }

    @Test
    @DisplayName("zero()가 0 값의 PointAmount를 반환한다")
    void shouldReturnZeroPointAmount() {
        PointAmount zero = PointAmount.zero();

        assertThat(zero.getValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("add()가 두 PointAmount의 합을 새 인스턴스로 반환한다")
    void shouldAddTwoPointAmounts() {
        PointAmount a = PointAmount.of(100L);
        PointAmount b = PointAmount.of(50L);

        PointAmount result = a.add(b);

        assertThat(result.getValue()).isEqualTo(150L);
        assertThat(a.getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("add()가 오버플로 시 ArithmeticException을 던진다")
    void shouldThrowArithmeticExceptionWhenAddOverflows() {
        PointAmount max = PointAmount.of(Long.MAX_VALUE);
        PointAmount one = PointAmount.of(1L);

        assertThatThrownBy(() -> max.add(one))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("subtract()가 두 PointAmount의 차를 새 인스턴스로 반환한다")
    void shouldSubtractTwoPointAmounts() {
        PointAmount a = PointAmount.of(100L);
        PointAmount b = PointAmount.of(30L);

        PointAmount result = a.subtract(b);

        assertThat(result.getValue()).isEqualTo(70L);
        assertThat(a.getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("subtract() 결과가 음수이면 InvalidPointAmountException을 던진다")
    void shouldThrowExceptionWhenSubtractResultIsNegative() {
        PointAmount a = PointAmount.of(30L);
        PointAmount b = PointAmount.of(50L);

        assertThatThrownBy(() -> a.subtract(b))
                .isInstanceOf(InvalidPointAmountException.class);
    }

    @Test
    @DisplayName("isZero()가 값이 0일 때 true를 반환한다")
    void shouldReturnTrueWhenIsZero() {
        assertThat(PointAmount.zero().isZero()).isTrue();
        assertThat(PointAmount.of(1L).isZero()).isFalse();
    }

    @Test
    @DisplayName("isPositive()가 값이 양수일 때 true를 반환한다")
    void shouldReturnTrueWhenIsPositive() {
        assertThat(PointAmount.of(1L).isPositive()).isTrue();
        assertThat(PointAmount.zero().isPositive()).isFalse();
    }

    @Test
    @DisplayName("isGreaterThan()이 현재 값이 인수 값보다 크면 true를 반환한다")
    void shouldReturnTrueWhenIsGreaterThan() {
        PointAmount a = PointAmount.of(100L);
        PointAmount b = PointAmount.of(50L);

        assertThat(a.isGreaterThan(b)).isTrue();
        assertThat(b.isGreaterThan(a)).isFalse();
        assertThat(a.isGreaterThan(a)).isFalse();
    }

    @Test
    @DisplayName("isGreaterThanOrEqual()이 현재 값이 인수 값 이상이면 true를 반환한다")
    void shouldReturnTrueWhenIsGreaterThanOrEqual() {
        PointAmount a = PointAmount.of(100L);
        PointAmount b = PointAmount.of(50L);
        PointAmount c = PointAmount.of(100L);

        assertThat(a.isGreaterThanOrEqual(b)).isTrue();
        assertThat(a.isGreaterThanOrEqual(c)).isTrue();
        assertThat(b.isGreaterThanOrEqual(a)).isFalse();
    }

    @Test
    @DisplayName("isLessThan()이 현재 값이 인수 값보다 작으면 true를 반환한다")
    void shouldReturnTrueWhenIsLessThan() {
        PointAmount a = PointAmount.of(50L);
        PointAmount b = PointAmount.of(100L);

        assertThat(a.isLessThan(b)).isTrue();
        assertThat(b.isLessThan(a)).isFalse();
        assertThat(a.isLessThan(a)).isFalse();
    }

    @Test
    @DisplayName("equals()와 hashCode()가 값 기반으로 동작한다")
    void shouldImplementEqualsAndHashCodeByValue() {
        PointAmount a = PointAmount.of(100L);
        PointAmount b = PointAmount.of(100L);
        PointAmount c = PointAmount.of(200L);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("equals()는 null과 다른 타입에 대해 false, 자기 자신에 대해 true를 반환한다")
    void shouldImplementEqualsContract() {
        PointAmount a = PointAmount.of(100L);

        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("100")).isFalse();
        assertThat(a.equals(a)).isTrue();
    }
}
