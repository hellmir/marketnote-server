package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.NICKNAME_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

class NicknamePatternTest {

    private final Pattern pattern = Pattern.compile(NICKNAME_PATTERN);

    private boolean isValid(String value) {
        return FormatValidator.isValid(value, pattern);
    }

    @Nested
    @DisplayName("검증 성공")
    class 검증_성공 {

        @Test
        @DisplayName("한글 2자 닉네임은 검증에 성공한다")
        void shouldPassKorean2Characters() {
            assertThat(isValid("가나")).isTrue();
        }

        @Test
        @DisplayName("영어 대소문자 닉네임은 검증에 성공한다")
        void shouldPassEnglishUpperAndLowerCase() {
            assertThat(isValid("AbcDef")).isTrue();
        }

        @Test
        @DisplayName("숫자 포함 닉네임은 검증에 성공한다")
        void shouldPassWithNumbers() {
            assertThat(isValid("user123")).isTrue();
        }

        @Test
        @DisplayName("한글, 영어, 숫자 혼합 닉네임은 검증에 성공한다")
        void shouldPassMixedCharacters() {
            assertThat(isValid("유저user1")).isTrue();
        }

        @Test
        @DisplayName("10자 닉네임은 검증에 성공한다")
        void shouldPassMaxLength10() {
            assertThat(isValid("abcdefghij")).isTrue();
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class 검증_실패 {

        @Test
        @DisplayName("1자 닉네임은 검증에 실패한다")
        void shouldFailMinLength1() {
            assertThat(isValid("가")).isFalse();
        }

        @Test
        @DisplayName("11자 닉네임은 검증에 실패한다")
        void shouldFailMaxLength11() {
            assertThat(isValid("abcdefghijk")).isFalse();
        }

        @Test
        @DisplayName("특수문자 포함 닉네임은 검증에 실패한다")
        void shouldFailWithSpecialCharacters() {
            assertThat(isValid("user!@#")).isFalse();
        }

        @Test
        @DisplayName("공백 포함 닉네임은 검증에 실패한다")
        void shouldFailWithSpaces() {
            assertThat(isValid("user name")).isFalse();
        }
    }
}
