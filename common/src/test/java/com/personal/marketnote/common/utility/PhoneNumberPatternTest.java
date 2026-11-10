package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.PHONE_NUMBER_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberPatternTest {

    private final Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);

    private boolean isValid(String value) {
        return FormatValidator.isValid(value, pattern);
    }

    @Nested
    @DisplayName("검증 성공")
    class 검증_성공 {

        @ParameterizedTest(name = "canonical 11자리 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "01012345678",
                "01112345678",
                "01612345678",
                "01712345678",
                "01812345678",
                "01912345678"
        })
        @DisplayName("허용 prefix + canonical 11자리 형식은 검증에 성공한다")
        void shouldPassCanonicalElevenDigitsWithAllPrefixes(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "canonical 10자리 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "0101234567",
                "0111234567",
                "0161234567",
                "0171234567",
                "0181234567",
                "0191234567"
        })
        @DisplayName("허용 prefix + canonical 10자리 형식은 검증에 성공한다")
        void shouldPassCanonicalTenDigits(String value) {
            assertThat(isValid(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class 검증_실패 {

        @ParameterizedTest(name = "하이픈 포함 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010-1234-5678",
                "011-123-4567",
                "010-12345678",
                "0101234-5678"
        })
        @DisplayName("하이픈 포함 형식은 모두 검증에 실패한다")
        void shouldFailWhenContainsHyphen(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "비허용 prefix {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "0121234567",
                "01212345678"
        })
        @DisplayName("허용되지 않는 prefix 는 검증에 실패한다")
        void shouldFailWhenInvalidPrefix(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "자릿수 오류 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "0101234",
                "010123456",
                "010123456789"
        })
        @DisplayName("canonical 형식 자릿수 오류는 검증에 실패한다")
        void shouldFailWhenLengthInvalid(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "숫자 외 문자 포함 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010abcd5678",
                "010.1234.5678",
                " 01012345678"
        })
        @DisplayName("숫자 외 문자를 포함한 형식은 검증에 실패한다")
        void shouldFailWhenContainsNonDigit(String value) {
            assertThat(isValid(value)).isFalse();
        }
    }
}
