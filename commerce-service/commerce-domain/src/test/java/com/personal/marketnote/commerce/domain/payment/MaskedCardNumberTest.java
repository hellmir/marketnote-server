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

        @Test
        @DisplayName("정확히 12자(최소 길이)로 생성 시 정상 생성된다")
        void shouldCreateWithExactlyMinLength() {
            String value = "1234********";

            MaskedCardNumber maskedCardNumber = MaskedCardNumber.of(value);

            assertThat(maskedCardNumber.getValue()).isEqualTo(value);
            assertThat(value).hasSize(12);
        }

        @Test
        @DisplayName("정확히 20자(최대 길이)로 생성 시 정상 생성된다")
        void shouldCreateWithExactlyMaxLength() {
            String value = "12345-****-****-1234";

            MaskedCardNumber maskedCardNumber = MaskedCardNumber.of(value);

            assertThat(maskedCardNumber.getValue()).isEqualTo(value);
            assertThat(value).hasSize(20);
        }

        @Test
        @DisplayName("11자(최소 길이 미만)로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenLengthBelowMin() {
            String value = "123*******1";

            assertThat(value).hasSize(11);
            assertThatThrownBy(() -> MaskedCardNumber.of(value))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }

        @Test
        @DisplayName("21자(최대 길이 초과)로 생성 시 InvalidMaskedCardNumberException을 던진다")
        void shouldThrowWhenLengthAboveMax() {
            String value = "12345-****-****-12345";

            assertThat(value).hasSize(21);
            assertThatThrownBy(() -> MaskedCardNumber.of(value))
                    .isInstanceOf(InvalidMaskedCardNumberException.class);
        }
    }

    @Nested
    @DisplayName("toNullableValue(MaskedCardNumber) 정적 헬퍼")
    class ToNullableValueHelper {

        @Test
        @DisplayName("MaskedCardNumber가 null이면 null을 반환한다")
        void shouldReturnNullWhenNull() {
            assertThat(MaskedCardNumber.toNullableValue(null)).isNull();
        }

        @Test
        @DisplayName("MaskedCardNumber가 있으면 내부 String 값을 반환한다")
        void shouldReturnValueWhenPresent() {
            MaskedCardNumber maskedCardNumber = MaskedCardNumber.of("1234-****-****-5678");

            assertThat(MaskedCardNumber.toNullableValue(maskedCardNumber)).isEqualTo("1234-****-****-5678");
        }
    }

    @Nested
    @DisplayName("fromNullable(String) 정적 헬퍼")
    class FromNullableHelper {

        @Test
        @DisplayName("입력이 null이면 null을 반환한다")
        void shouldReturnNullWhenInputNull() {
            assertThat(MaskedCardNumber.fromNullable(null)).isNull();
        }

        @Test
        @DisplayName("입력이 빈 문자열이면 null을 반환한다")
        void shouldReturnNullWhenInputEmpty() {
            assertThat(MaskedCardNumber.fromNullable("")).isNull();
        }

        @Test
        @DisplayName("입력이 공백 문자열이면 null을 반환한다")
        void shouldReturnNullWhenInputBlank() {
            assertThat(MaskedCardNumber.fromNullable("   ")).isNull();
        }

        @Test
        @DisplayName("유효한 마스킹 카드번호 입력 시 VO를 반환한다")
        void shouldReturnVoWhenValidInput() {
            MaskedCardNumber result = MaskedCardNumber.fromNullable("1234-****-****-5678");

            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("1234-****-****-5678");
        }

        @Test
        @DisplayName("유효하지 않은 입력 시 InvalidMaskedCardNumberException을 전파한다")
        void shouldPropagateExceptionWhenInputInvalid() {
            assertThatThrownBy(() -> MaskedCardNumber.fromNullable("invalid"))
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
