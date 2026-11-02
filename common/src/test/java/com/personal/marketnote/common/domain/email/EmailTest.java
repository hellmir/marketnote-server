package com.personal.marketnote.common.domain.email;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidEmailException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.EmailNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 이메일(user@example.com)로 생성 시 정상 생성된다")
        void shouldCreateEmailWithValidFormat() {
            Email email = Email.of("user@example.com");

            assertThat(email.getValue()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("@ 없는 문자열로 생성 시 InvalidEmailException을 던진다")
        void shouldThrowWhenMissingAtSymbol() {
            assertThatThrownBy(() -> Email.of("userexample.com"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("도메인 없는 이메일(user@)로 생성 시 InvalidEmailException을 던진다")
        void shouldThrowWhenMissingDomain() {
            assertThatThrownBy(() -> Email.of("user@"))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("null로 생성 시 EmailNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(EmailNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 EmailNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> Email.of(""))
                    .isInstanceOf(EmailNoValueException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            Email a = Email.of("user@example.com");
            Email b = Email.of("user@example.com");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            Email a = Email.of("user@example.com");
            Email b = Email.of("other@example.com");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            Email email = Email.of("user@example.com");

            assertThat(email.toString()).doesNotContain("user@example.com");
            assertThat(email.toString()).contains("***");
        }
    }
}
