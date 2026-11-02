package com.personal.marketnote.common.domain.recipientname;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidRecipientNameException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.RecipientNameNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecipientNameTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 한글 수령인명으로 생성 시 정상 생성된다")
        void shouldCreateRecipientNameWithKorean() {
            RecipientName recipientName = RecipientName.of("홍길동");

            assertThat(recipientName.getValue()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("유효한 영문 수령인명으로 생성 시 정상 생성된다")
        void shouldCreateRecipientNameWithEnglish() {
            RecipientName recipientName = RecipientName.of("John Doe");

            assertThat(recipientName.getValue()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("null로 생성 시 RecipientNameNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> RecipientName.of(null))
                    .isInstanceOf(RecipientNameNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 RecipientNameNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> RecipientName.of(""))
                    .isInstanceOf(RecipientNameNoValueException.class);
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 시 RecipientNameNoValueException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> RecipientName.of("   "))
                    .isInstanceOf(RecipientNameNoValueException.class);
        }

        @Test
        @DisplayName("숫자가 포함된 수령인명으로 생성 시 InvalidRecipientNameException을 던진다")
        void shouldThrowWhenContainsNumber() {
            assertThatThrownBy(() -> RecipientName.of("홍길동123"))
                    .isInstanceOf(InvalidRecipientNameException.class);
        }

        @Test
        @DisplayName("특수문자가 포함된 수령인명으로 생성 시 InvalidRecipientNameException을 던진다")
        void shouldThrowWhenContainsSpecialCharacter() {
            assertThatThrownBy(() -> RecipientName.of("홍길동!"))
                    .isInstanceOf(InvalidRecipientNameException.class);
        }

        @Test
        @DisplayName("50자 초과 수령인명으로 생성 시 InvalidRecipientNameException을 던진다")
        void shouldThrowWhenExceedsMaxLength() {
            String tooLong = "가".repeat(51);

            assertThatThrownBy(() -> RecipientName.of(tooLong))
                    .isInstanceOf(InvalidRecipientNameException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            RecipientName a = RecipientName.of("홍길동");
            RecipientName b = RecipientName.of("홍길동");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            RecipientName a = RecipientName.of("홍길동");
            RecipientName b = RecipientName.of("이순신");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            RecipientName recipientName = RecipientName.of("홍길동");

            assertThat(recipientName.toString()).doesNotContain("홍길동");
            assertThat(recipientName.toString()).contains("***");
        }
    }
}
