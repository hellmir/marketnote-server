package com.personal.marketnote.common.domain.phonenumber;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidPhoneNumberException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.PhoneNumberNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("canonical 11자리(01012345678)로 생성 시 정상 생성된다")
        void shouldCreatePhoneNumberWithCanonicalElevenDigits() {
            PhoneNumber phoneNumber = PhoneNumber.of("01012345678");

            assertThat(phoneNumber.getValue()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("canonical 10자리(0101234567)로 생성 시 정상 생성된다")
        void shouldCreatePhoneNumberWithCanonicalTenDigits() {
            PhoneNumber phoneNumber = PhoneNumber.of("0101234567");

            assertThat(phoneNumber.getValue()).isEqualTo("0101234567");
        }

        @Test
        @DisplayName("하이픈 포함(010-1234-5678)으로 생성 시 하이픈이 제거된 canonical 값으로 생성된다")
        void shouldCreateCanonicalFromFullyHyphenatedInput() {
            PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");

            assertThat(phoneNumber.getValue()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("하이픈 부분 포함(010-12345678)으로 생성 시 하이픈이 제거된 canonical 값으로 생성된다")
        void shouldCreateCanonicalFromPartiallyHyphenatedInput() {
            PhoneNumber phoneNumber = PhoneNumber.of("010-12345678");

            assertThat(phoneNumber.getValue()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("하이픈 혼용(0101234-5678)으로 생성 시 하이픈이 제거된 canonical 값으로 생성된다")
        void shouldCreateCanonicalFromMixedHyphenatedInput() {
            PhoneNumber phoneNumber = PhoneNumber.of("0101234-5678");

            assertThat(phoneNumber.getValue()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("10자리 하이픈 포함(010-123-4567)으로 생성 시 하이픈이 제거된 canonical 값으로 생성된다")
        void shouldCreateCanonicalFromHyphenatedTenDigits() {
            PhoneNumber phoneNumber = PhoneNumber.of("010-123-4567");

            assertThat(phoneNumber.getValue()).isEqualTo("0101234567");
        }

        @Test
        @DisplayName("하이픈 제거 후에도 자릿수가 부족한 값(010-1234-56)이면 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenHyphenRemovedStillInvalid() {
            assertThatThrownBy(() -> PhoneNumber.of("010-1234-56"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("비허용 앞자리(01212345678)로 생성 시 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenInvalidPrefix() {
            assertThatThrownBy(() -> PhoneNumber.of("01212345678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("공백 포함 입력(010 1234 5678)은 하이픈만 제거하므로 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenSpaceDelimiterIncluded() {
            assertThatThrownBy(() -> PhoneNumber.of("010 1234 5678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("괄호 포함 입력((010)1234-5678)은 하이픈만 제거하므로 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenParenthesisIncluded() {
            assertThatThrownBy(() -> PhoneNumber.of("(010)1234-5678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("온점 포함 입력(010.1234.5678)은 하이픈만 제거하므로 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenDotDelimiterIncluded() {
            assertThatThrownBy(() -> PhoneNumber.of("010.1234.5678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("null로 생성 시 PhoneNumberNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> PhoneNumber.of(null))
                    .isInstanceOf(PhoneNumberNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 PhoneNumberNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> PhoneNumber.of(""))
                    .isInstanceOf(PhoneNumberNoValueException.class);
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 시 PhoneNumberNoValueException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> PhoneNumber.of("   "))
                    .isInstanceOf(PhoneNumberNoValueException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 canonical 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            PhoneNumber a = PhoneNumber.of("01012345678");
            PhoneNumber b = PhoneNumber.of("01012345678");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 canonical 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            PhoneNumber a = PhoneNumber.of("01012345678");
            PhoneNumber b = PhoneNumber.of("01012345679");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            PhoneNumber phoneNumber = PhoneNumber.of("01012345678");

            assertThat(phoneNumber.toString()).doesNotContain("01012345678");
            assertThat(phoneNumber.toString()).contains("***");
        }
    }
}
