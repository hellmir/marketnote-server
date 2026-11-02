package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.user.domain.user.exception.InvalidNicknameException;
import com.personal.marketnote.user.domain.user.exception.NicknameNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NicknameTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 한글 닉네임으로 생성 시 정상 생성된다")
        void shouldCreateNicknameWithKorean() {
            Nickname nickname = Nickname.of("홍길동");

            assertThat(nickname.getValue()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("유효한 영문 닉네임으로 생성 시 정상 생성된다")
        void shouldCreateNicknameWithEnglish() {
            Nickname nickname = Nickname.of("JohnDoe");

            assertThat(nickname.getValue()).isEqualTo("JohnDoe");
        }

        @Test
        @DisplayName("유효한 영문+숫자 닉네임으로 생성 시 정상 생성된다")
        void shouldCreateNicknameWithAlphanumeric() {
            Nickname nickname = Nickname.of("user123");

            assertThat(nickname.getValue()).isEqualTo("user123");
        }

        @Test
        @DisplayName("null로 생성 시 NicknameNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> Nickname.of(null))
                    .isInstanceOf(NicknameNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 NicknameNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> Nickname.of(""))
                    .isInstanceOf(NicknameNoValueException.class);
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 시 NicknameNoValueException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> Nickname.of("   "))
                    .isInstanceOf(NicknameNoValueException.class);
        }

        @Test
        @DisplayName("2자 미만 닉네임으로 생성 시 InvalidNicknameException을 던진다")
        void shouldThrowWhenTooShort() {
            assertThatThrownBy(() -> Nickname.of("a"))
                    .isInstanceOf(InvalidNicknameException.class);
        }

        @Test
        @DisplayName("10자 초과 닉네임으로 생성 시 InvalidNicknameException을 던진다")
        void shouldThrowWhenExceedsMaxLength() {
            String tooLong = "a".repeat(11);

            assertThatThrownBy(() -> Nickname.of(tooLong))
                    .isInstanceOf(InvalidNicknameException.class);
        }

        @Test
        @DisplayName("특수문자가 포함된 닉네임으로 생성 시 InvalidNicknameException을 던진다")
        void shouldThrowWhenContainsSpecialCharacter() {
            assertThatThrownBy(() -> Nickname.of("hello!"))
                    .isInstanceOf(InvalidNicknameException.class);
        }

        @Test
        @DisplayName("공백이 포함된 닉네임으로 생성 시 InvalidNicknameException을 던진다")
        void shouldThrowWhenContainsSpace() {
            assertThatThrownBy(() -> Nickname.of("hello world"))
                    .isInstanceOf(InvalidNicknameException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            Nickname a = Nickname.of("홍길동");
            Nickname b = Nickname.of("홍길동");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            Nickname a = Nickname.of("홍길동");
            Nickname b = Nickname.of("이순신");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            Nickname nickname = Nickname.of("홍길동");

            assertThat(nickname.toString()).doesNotContain("홍길동");
            assertThat(nickname.toString()).contains("***");
        }
    }
}
