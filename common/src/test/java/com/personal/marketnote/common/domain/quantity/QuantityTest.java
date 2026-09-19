package com.personal.marketnote.common.domain.quantity;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.QuantityNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Nested
    @DisplayName("of(int) 팩토리 메서드")
    class OfIntPrimitive {

        @Test
        @DisplayName("수량 1로 Quantity를 생성한다")
        void shouldCreateQuantityWithOne() {
            Quantity quantity = Quantity.of(1);

            assertThat(quantity.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("양수 수량으로 Quantity를 생성한다")
        void shouldCreateQuantityWithPositiveValue() {
            Quantity quantity = Quantity.of(100);

            assertThat(quantity.getValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("수량이 0이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenValueIsZero() {
            assertThatThrownBy(() -> Quantity.of(0))
                    .isInstanceOf(InvalidQuantityException.class);
        }

        @Test
        @DisplayName("수량이 음수이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenValueIsNegative() {
            assertThatThrownBy(() -> Quantity.of(-1))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("of(Integer) 팩토리 메서드")
    class OfIntegerWrapper {

        @Test
        @DisplayName("양수 Integer로 Quantity를 생성한다")
        void shouldCreateQuantityWithPositiveInteger() {
            Quantity quantity = Quantity.of(Integer.valueOf(50));

            assertThat(quantity.getValue()).isEqualTo(50);
        }

        @Test
        @DisplayName("null이면 QuantityNoValueException을 던진다")
        void shouldThrowExceptionWhenValueIsNull() {
            assertThatThrownBy(() -> Quantity.of((Integer) null))
                    .isInstanceOf(QuantityNoValueException.class);
        }

        @Test
        @DisplayName("Integer 값이 0이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenIntegerIsZero() {
            assertThatThrownBy(() -> Quantity.of(Integer.valueOf(0)))
                    .isInstanceOf(InvalidQuantityException.class);
        }

        @Test
        @DisplayName("Integer 값이 음수이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenIntegerIsNegative() {
            assertThatThrownBy(() -> Quantity.of(Integer.valueOf(-10)))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("add 연산")
    class Add {

        @Test
        @DisplayName("두 Quantity를 더한 새 인스턴스를 반환한다")
        void shouldReturnNewQuantityWithAddedValue() {
            Quantity quantity1 = Quantity.of(3);
            Quantity quantity2 = Quantity.of(5);

            Quantity result = quantity1.add(quantity2);

            assertThat(result.getValue()).isEqualTo(8);
            assertThat(quantity1.getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("오버플로 시 ArithmeticException을 던진다")
        void shouldThrowExceptionWhenAdditionOverflows() {
            Quantity quantity1 = Quantity.of(Integer.MAX_VALUE);
            Quantity quantity2 = Quantity.of(1);

            assertThatThrownBy(() -> quantity1.add(quantity2))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("subtract 연산")
    class Subtract {

        @Test
        @DisplayName("차감한 새 Quantity를 반환한다")
        void shouldReturnNewQuantityWithSubtractedValue() {
            Quantity quantity1 = Quantity.of(10);
            Quantity quantity2 = Quantity.of(3);

            Quantity result = quantity1.subtract(quantity2);

            assertThat(result.getValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("차감 결과가 0이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenSubtractionResultIsZero() {
            Quantity quantity1 = Quantity.of(5);
            Quantity quantity2 = Quantity.of(5);

            assertThatThrownBy(() -> quantity1.subtract(quantity2))
                    .isInstanceOf(InvalidQuantityException.class);
        }

        @Test
        @DisplayName("차감 결과가 음수이면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenSubtractionResultIsNegative() {
            Quantity quantity1 = Quantity.of(3);
            Quantity quantity2 = Quantity.of(5);

            assertThatThrownBy(() -> quantity1.subtract(quantity2))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("multiply 연산")
    class Multiply {

        @Test
        @DisplayName("곱한 새 Quantity를 반환한다")
        void shouldReturnNewQuantityWithMultipliedValue() {
            Quantity quantity = Quantity.of(4);

            Quantity result = quantity.multiply(3);

            assertThat(result.getValue()).isEqualTo(12);
        }

        @Test
        @DisplayName("오버플로 시 ArithmeticException을 던진다")
        void shouldThrowExceptionWhenMultiplicationOverflows() {
            Quantity quantity = Quantity.of(Integer.MAX_VALUE);

            assertThatThrownBy(() -> quantity.multiply(2))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("0을 곱하면 InvalidQuantityException을 던진다")
        void shouldThrowExceptionWhenMultipliedByZero() {
            Quantity quantity = Quantity.of(5);

            assertThatThrownBy(() -> quantity.multiply(0))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }

    @Nested
    @DisplayName("비교 메서드")
    class Comparison {

        @Test
        @DisplayName("isGreaterThan은 더 큰 수량일 때 true를 반환한다")
        void shouldReturnTrueWhenGreaterThan() {
            Quantity bigger = Quantity.of(10);
            Quantity smaller = Quantity.of(5);

            assertThat(bigger.isGreaterThan(smaller)).isTrue();
            assertThat(smaller.isGreaterThan(bigger)).isFalse();
            assertThat(bigger.isGreaterThan(Quantity.of(10))).isFalse();
        }

        @Test
        @DisplayName("isGreaterThanOrEqual은 같거나 큰 수량일 때 true를 반환한다")
        void shouldReturnTrueWhenGreaterThanOrEqual() {
            Quantity quantity = Quantity.of(10);

            assertThat(quantity.isGreaterThanOrEqual(Quantity.of(5))).isTrue();
            assertThat(quantity.isGreaterThanOrEqual(Quantity.of(10))).isTrue();
            assertThat(quantity.isGreaterThanOrEqual(Quantity.of(20))).isFalse();
        }

        @Test
        @DisplayName("isLessThan은 더 작은 수량일 때 true를 반환한다")
        void shouldReturnTrueWhenLessThan() {
            Quantity smaller = Quantity.of(5);
            Quantity bigger = Quantity.of(10);

            assertThat(smaller.isLessThan(bigger)).isTrue();
            assertThat(bigger.isLessThan(smaller)).isFalse();
            assertThat(smaller.isLessThan(Quantity.of(5))).isFalse();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 값의 Quantity는 동등하다")
        void shouldBeEqualWhenSameValue() {
            Quantity quantity1 = Quantity.of(10);
            Quantity quantity2 = Quantity.of(10);

            assertThat(quantity1).isEqualTo(quantity2);
            assertThat(quantity1.hashCode()).isEqualTo(quantity2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 Quantity는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            Quantity quantity1 = Quantity.of(10);
            Quantity quantity2 = Quantity.of(20);

            assertThat(quantity1).isNotEqualTo(quantity2);
        }

        @Test
        @DisplayName("null과 비교하면 동등하지 않다")
        void shouldNotBeEqualToNull() {
            Quantity quantity = Quantity.of(10);

            assertThat(quantity).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교하면 동등하지 않다")
        void shouldNotBeEqualToDifferentType() {
            Quantity quantity = Quantity.of(10);

            assertThat(quantity).isNotEqualTo(10);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("수량 값을 포함한 문자열을 반환한다")
        void shouldReturnStringContainingValue() {
            Quantity quantity = Quantity.of(42);

            assertThat(quantity.toString()).contains("42");
        }
    }
}
