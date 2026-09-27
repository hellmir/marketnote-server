package com.personal.marketnote.commerce.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstallmentTest {

    @Nested
    @DisplayName("of(short) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("0(일시불)으로 생성 시 정상 생성되고 isLumpSum()이 true를 반환한다")
        void shouldCreateLumpSumWithZero() {
            Installment installment = Installment.of((short) 0);

            assertThat(installment.getValue()).isEqualTo((short) 0);
            assertThat(installment.isLumpSum()).isTrue();
        }

        @Test
        @DisplayName("2개월로 생성 시 정상 생성되고 isLumpSum()이 false를 반환한다")
        void shouldCreateInstallmentWithTwoMonths() {
            Installment installment = Installment.of((short) 2);

            assertThat(installment.getValue()).isEqualTo((short) 2);
            assertThat(installment.isLumpSum()).isFalse();
        }

        @Test
        @DisplayName("36개월로 생성 시 정상 생성된다")
        void shouldCreateInstallmentWithThirtySixMonths() {
            Installment installment = Installment.of((short) 36);

            assertThat(installment.getValue()).isEqualTo((short) 36);
            assertThat(installment.isLumpSum()).isFalse();
        }

        @ParameterizedTest(name = "{0}개월로 생성 시 InvalidInstallmentException을 던진다")
        @ValueSource(shorts = {-1, 1, 37, 100})
        @DisplayName("허용 범위를 벗어난 값으로 생성 시 InvalidInstallmentException을 던진다")
        void shouldThrowWhenOutOfRange(short invalid) {
            assertThatThrownBy(() -> Installment.of(invalid))
                    .isInstanceOf(InvalidInstallmentException.class);
        }
    }

    @Nested
    @DisplayName("toNullableValue(Installment) 정적 헬퍼")
    class ToNullableValueHelper {

        @Test
        @DisplayName("Installment가 null이면 null을 반환한다")
        void shouldReturnNullWhenNull() {
            assertThat(Installment.toNullableValue(null)).isNull();
        }

        @Test
        @DisplayName("Installment가 일시불(0)이면 0을 반환한다")
        void shouldReturnZeroWhenLumpSum() {
            Installment installment = Installment.of((short) 0);

            assertThat(Installment.toNullableValue(installment)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("Installment가 할부면 개월 수를 반환한다")
        void shouldReturnMonthsWhenInstallment() {
            Installment installment = Installment.of((short) 6);

            assertThat(Installment.toNullableValue(installment)).isEqualTo((short) 6);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            Installment a = Installment.of((short) 3);
            Installment b = Installment.of((short) 3);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            Installment a = Installment.of((short) 3);
            Installment b = Installment.of((short) 6);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
