package com.personal.marketnote.common.domain.money;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidMoneyAmountException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.MoneyAmountNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("of(long) 팩토리 메서드")
    class OfLongPrimitive {

        @Test
        @DisplayName("양수로 Money를 생성한다")
        void shouldCreateMoneyWithPositiveAmount() {
            Money money = Money.of(1000L);

            assertThat(money.getValue()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("0으로 Money를 생성한다")
        void shouldCreateMoneyWithZero() {
            Money money = Money.of(0L);

            assertThat(money.getValue()).isZero();
        }

        @Test
        @DisplayName("음수이면 InvalidMoneyAmountException을 던진다")
        void shouldThrowExceptionWhenAmountIsNegative() {
            assertThatThrownBy(() -> Money.of(-1L))
                    .isInstanceOf(InvalidMoneyAmountException.class);
        }
    }

    @Nested
    @DisplayName("of(Long) 팩토리 메서드")
    class OfLongWrapper {

        @Test
        @DisplayName("양수 Long으로 Money를 생성한다")
        void shouldCreateMoneyWithPositiveLong() {
            Money money = Money.of(Long.valueOf(5000L));

            assertThat(money.getValue()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("null이면 MoneyAmountNoValueException을 던진다")
        void shouldThrowExceptionWhenAmountIsNull() {
            assertThatThrownBy(() -> Money.of((Long) null))
                    .isInstanceOf(MoneyAmountNoValueException.class);
        }

        @Test
        @DisplayName("음수 Long이면 InvalidMoneyAmountException을 던진다")
        void shouldThrowExceptionWhenLongAmountIsNegative() {
            assertThatThrownBy(() -> Money.of(Long.valueOf(-100L)))
                    .isInstanceOf(InvalidMoneyAmountException.class);
        }
    }

    @Nested
    @DisplayName("zero() 팩토리 메서드")
    class Zero {

        @Test
        @DisplayName("0원 Money를 생성한다")
        void shouldCreateZeroMoney() {
            Money money = Money.zero();

            assertThat(money.getValue()).isZero();
            assertThat(money.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("add 연산")
    class Add {

        @Test
        @DisplayName("두 Money를 더한 새 인스턴스를 반환한다")
        void shouldReturnNewMoneyWithAddedAmount() {
            Money money1 = Money.of(1000L);
            Money money2 = Money.of(2000L);

            Money result = money1.add(money2);

            assertThat(result.getValue()).isEqualTo(3000L);
            assertThat(money1.getValue()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("오버플로 시 ArithmeticException을 던진다")
        void shouldThrowExceptionWhenAdditionOverflows() {
            Money money1 = Money.of(Long.MAX_VALUE);
            Money money2 = Money.of(1L);

            assertThatThrownBy(() -> money1.add(money2))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("subtract 연산")
    class Subtract {

        @Test
        @DisplayName("차감한 새 Money를 반환한다")
        void shouldReturnNewMoneyWithSubtractedAmount() {
            Money money1 = Money.of(3000L);
            Money money2 = Money.of(1000L);

            Money result = money1.subtract(money2);

            assertThat(result.getValue()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("결과가 음수이면 InvalidMoneyAmountException을 던진다")
        void shouldThrowExceptionWhenSubtractionResultIsNegative() {
            Money money1 = Money.of(1000L);
            Money money2 = Money.of(2000L);

            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(InvalidMoneyAmountException.class);
        }

        @Test
        @DisplayName("동일 금액을 차감하면 0원이 된다")
        void shouldReturnZeroWhenSubtractingSameAmount() {
            Money money = Money.of(5000L);

            Money result = money.subtract(Money.of(5000L));

            assertThat(result.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("multiply 연산")
    class Multiply {

        @Test
        @DisplayName("곱한 새 Money를 반환한다")
        void shouldReturnNewMoneyWithMultipliedAmount() {
            Money money = Money.of(1000L);

            Money result = money.multiply(3);

            assertThat(result.getValue()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("0을 곱하면 0원이 된다")
        void shouldReturnZeroWhenMultipliedByZero() {
            Money money = Money.of(1000L);

            Money result = money.multiply(0);

            assertThat(result.isZero()).isTrue();
        }

        @Test
        @DisplayName("오버플로 시 ArithmeticException을 던진다")
        void shouldThrowExceptionWhenMultiplicationOverflows() {
            Money money = Money.of(Long.MAX_VALUE);

            assertThatThrownBy(() -> money.multiply(2))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("비교 메서드")
    class Comparison {

        @Test
        @DisplayName("isZero는 0원일 때 true를 반환한다")
        void shouldReturnTrueWhenAmountIsZero() {
            assertThat(Money.zero().isZero()).isTrue();
            assertThat(Money.of(1L).isZero()).isFalse();
        }

        @Test
        @DisplayName("isPositive는 양수일 때 true를 반환한다")
        void shouldReturnTrueWhenAmountIsPositive() {
            assertThat(Money.of(1L).isPositive()).isTrue();
            assertThat(Money.zero().isPositive()).isFalse();
        }

        @Test
        @DisplayName("isGreaterThan은 더 큰 금액일 때 true를 반환한다")
        void shouldReturnTrueWhenGreaterThan() {
            Money bigger = Money.of(2000L);
            Money smaller = Money.of(1000L);

            assertThat(bigger.isGreaterThan(smaller)).isTrue();
            assertThat(smaller.isGreaterThan(bigger)).isFalse();
            assertThat(bigger.isGreaterThan(Money.of(2000L))).isFalse();
        }

        @Test
        @DisplayName("isGreaterThanOrEqual은 같거나 큰 금액일 때 true를 반환한다")
        void shouldReturnTrueWhenGreaterThanOrEqual() {
            Money money = Money.of(2000L);

            assertThat(money.isGreaterThanOrEqual(Money.of(1000L))).isTrue();
            assertThat(money.isGreaterThanOrEqual(Money.of(2000L))).isTrue();
            assertThat(money.isGreaterThanOrEqual(Money.of(3000L))).isFalse();
        }

        @Test
        @DisplayName("isLessThan은 더 작은 금액일 때 true를 반환한다")
        void shouldReturnTrueWhenLessThan() {
            Money smaller = Money.of(1000L);
            Money bigger = Money.of(2000L);

            assertThat(smaller.isLessThan(bigger)).isTrue();
            assertThat(bigger.isLessThan(smaller)).isFalse();
            assertThat(smaller.isLessThan(Money.of(1000L))).isFalse();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 금액의 Money는 동등하다")
        void shouldBeEqualWhenSameAmount() {
            Money money1 = Money.of(1000L);
            Money money2 = Money.of(1000L);

            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 금액의 Money는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentAmount() {
            Money money1 = Money.of(1000L);
            Money money2 = Money.of(2000L);

            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("null과 비교하면 동등하지 않다")
        void shouldNotBeEqualToNull() {
            Money money = Money.of(1000L);

            assertThat(money).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교하면 동등하지 않다")
        void shouldNotBeEqualToDifferentType() {
            Money money = Money.of(1000L);

            assertThat(money).isNotEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("금액 값을 포함한 문자열을 반환한다")
        void shouldReturnStringContainingAmount() {
            Money money = Money.of(1000L);

            assertThat(money.toString()).contains("1000");
        }
    }
}
