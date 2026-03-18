package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueMaskerTest {

    @Test
    @DisplayName("null 입력 시 null을 반환한다")
    void shouldReturnNullWhenInputIsNull() {
        // given
        String input = null;

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 입력 시 빈 문자열을 반환한다")
    void shouldReturnEmptyStringWhenInputIsEmpty() {
        // given
        String input = "";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("2자 닉네임은 앞 1자와 *로 마스킹된다")
    void shouldMaskTwoCharNicknameWithFirstCharAndOneAsterisk() {
        // given
        String input = "민수";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEqualTo("민*");
    }

    @Test
    @DisplayName("3자 닉네임은 앞 2자와 **로 마스킹된다")
    void shouldMaskThreeCharNicknameWithFirstTwoCharsAndTwoAsterisks() {
        // given
        String input = "홍길동";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEqualTo("홍길**");
    }

    @Test
    @DisplayName("4자 닉네임은 앞 2자와 **로 마스킹된다")
    void shouldMaskFourCharNicknameWithFirstTwoCharsAndTwoAsterisks() {
        // given
        String input = "독고진수";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEqualTo("독고**");
    }

    @Test
    @DisplayName("5자 닉네임은 앞 3자와 ***로 마스킹된다")
    void shouldMaskFiveCharNicknameWithFirstThreeCharsAndThreeAsterisks() {
        // given
        String input = "Hello";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEqualTo("Hel***");
    }

    @Test
    @DisplayName("10자 닉네임은 앞 3자와 ***로 마스킹된다")
    void shouldMaskTenCharNicknameWithFirstThreeCharsAndThreeAsterisks() {
        // given
        String input = "abcdefghij";

        // when
        String result = ValueMasker.mask(input);

        // then
        assertThat(result).isEqualTo("abc***");
    }
}
