package com.personal.marketnote.commerce.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaskedCardNumberTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @ParameterizedTest(name = "유효한 마스킹 카드번호 {0}로 생성 시 정상 생성된다")
        @ValueSource(strings = {
                "1234-****-****-5678",
                "1234****5678",
                "5200********1234",
                "123456******7890"
        })
        @DisplayName("유효한 마스킹 카드번호로 생성 시 정상 생성된다")
        void shouldCreateWithValidMaskedValue(String value) {
            MaskedCardNumber maskedCardNumber = MaskedCardNumber.of(value);

            assertThat(maskedCardNumber.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("비마스킹 카드번호로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenNoMaskCharacter() {
            assertThatThrownBy(() -> MaskedCardNumber.of("1234567812345678"))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @Test
        @DisplayName("null로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> MaskedCardNumber.of(null))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> MaskedCardNumber.of(""))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @Test
        @DisplayName("공백 문자열로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> MaskedCardNumber.of("   "))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @ParameterizedTest(name = "허용되지 않는 문자가 포함된 {0}로 생성 시 예외를 던진다")
        @ValueSource(strings = {
                "1234-ABCD-****-5678",
                "1234 **** **** 5678",
                "$$$$-****-****-5678"
        })
        @DisplayName("허용되지 않는 문자가 포함된 경우 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenContainsInvalidCharacter(String value) {
            assertThatThrownBy(() -> MaskedCardNumber.of(value))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @Test
        @DisplayName("길이가 너무 짧으면 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenTooShort() {
            assertThatThrownBy(() -> MaskedCardNumber.of("12****"))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            MaskedCardNumber a = MaskedCardNumber.of("1234-****-****-5678");
            MaskedCardNumber b = MaskedCardNumber.of("1234-****-****-5678");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            MaskedCardNumber a = MaskedCardNumber.of("1234-****-****-5678");
            MaskedCardNumber b = MaskedCardNumber.of("1234-****-****-9999");

            assertThat(a).isNotEqualTo(b);
        }
    }
}
