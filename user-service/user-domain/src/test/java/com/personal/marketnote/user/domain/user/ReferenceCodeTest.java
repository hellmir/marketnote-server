package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.user.domain.user.exception.InvalidReferenceCodeException;
import com.personal.marketnote.user.domain.user.exception.ReferenceCodeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceCodeTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 추천인 코드로 생성 시 정상 생성된다")
        void shouldCreateReferenceCode() {
            ReferenceCode referenceCode = ReferenceCode.of("A3B4C6");

            assertThat(referenceCode.getValue()).isEqualTo("A3B4C6");
        }

        @Test
        @DisplayName("숫자만 구성된 추천인 코드로 생성 시 정상 생성된다")
        void shouldCreateReferenceCodeWithDigitsOnly() {
            ReferenceCode referenceCode = ReferenceCode.of("123456");

            assertThat(referenceCode.getValue()).isEqualTo("123456");
        }

        @Test
        @DisplayName("영문 대문자만 구성된 추천인 코드로 생성 시 정상 생성된다")
        void shouldCreateReferenceCodeWithLettersOnly() {
            ReferenceCode referenceCode = ReferenceCode.of("ABCDEF");

            assertThat(referenceCode.getValue()).isEqualTo("ABCDEF");
        }

        @Test
        @DisplayName("null로 생성 시 ReferenceCodeNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> ReferenceCode.of(null))
                    .isInstanceOf(ReferenceCodeNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 ReferenceCodeNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> ReferenceCode.of(""))
                    .isInstanceOf(ReferenceCodeNoValueException.class);
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 시 ReferenceCodeNoValueException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> ReferenceCode.of("   "))
                    .isInstanceOf(ReferenceCodeNoValueException.class);
        }

        @Test
        @DisplayName("6자 미만 추천인 코드로 생성 시 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenTooShort() {
            assertThatThrownBy(() -> ReferenceCode.of("A3B4C"))
                    .isInstanceOf(InvalidReferenceCodeException.class);
        }

        @Test
        @DisplayName("6자 초과 추천인 코드로 생성 시 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenTooLong() {
            assertThatThrownBy(() -> ReferenceCode.of("A3B4C6D"))
                    .isInstanceOf(InvalidReferenceCodeException.class);
        }

        @Test
        @DisplayName("영문 소문자가 포함된 추천인 코드로 생성 시 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenLowercase() {
            assertThatThrownBy(() -> ReferenceCode.of("a3b4c6"))
                    .isInstanceOf(InvalidReferenceCodeException.class);
        }

        @Test
        @DisplayName("특수문자가 포함된 추천인 코드로 생성 시 InvalidReferenceCodeException을 던진다")
        void shouldThrowWhenSpecialCharacter() {
            assertThatThrownBy(() -> ReferenceCode.of("A3B4-6"))
                    .isInstanceOf(InvalidReferenceCodeException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            ReferenceCode a = ReferenceCode.of("A3B4C6");
            ReferenceCode b = ReferenceCode.of("A3B4C6");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            ReferenceCode a = ReferenceCode.of("A3B4C6");
            ReferenceCode b = ReferenceCode.of("D7E8F9");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            ReferenceCode referenceCode = ReferenceCode.of("A3B4C6");

            assertThat(referenceCode.toString()).doesNotContain("A3B4C6");
            assertThat(referenceCode.toString()).contains("***");
        }
    }
}
