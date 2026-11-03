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

        @ParameterizedTest(name = "하이픈 포함 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "010-1234-5678",
                "011-1234-5678",
                "016-1234-5678",
                "017-1234-5678",
                "018-1234-5678",
                "019-1234-5678"
        })
        @DisplayName("허용 prefix + 하이픈 포함 4자리 미들은 검증에 성공한다")
        void shouldPassHyphenatedWithAllPrefixes(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "하이픈 포함 미들 3자리 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "010-123-5678",
                "011-123-4567"
        })
        @DisplayName("하이픈 포함 미들 3자리 형식은 검증에 성공한다")
        void shouldPassHyphenatedWithShortMiddle(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "하이픈 미포함 11자리 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "01012345678",
                "01112345678",
                "01612345678",
                "01712345678",
                "01812345678",
                "01912345678"
        })
        @DisplayName("하이픈 미포함 11자리 형식은 검증에 성공한다")
        void shouldPassHyphenlessElevenDigits(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "하이픈 미포함 10자리 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "0101234567",
                "0111234567"
        })
        @DisplayName("하이픈 미포함 10자리 형식은 검증에 성공한다")
        void shouldPassHyphenlessTenDigits(String value) {
            assertThat(isValid(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class 검증_실패 {

        @ParameterizedTest(name = "혼용 형식 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010-12345678",
                "0101234-5678"
        })
        @DisplayName("하이픈 혼용 형식은 검증에 실패한다")
        void shouldFailWhenHyphenMixed(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "비허용 prefix {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "02-1234-5678",
                "012-1234-5678",
                "021234567890"
        })
        @DisplayName("허용되지 않는 prefix 는 검증에 실패한다")
        void shouldFailWhenInvalidPrefix(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "미들 자릿수 오류 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010-12-5678",
                "010-12345-5678"
        })
        @DisplayName("하이픈 포함 형식 미들 자릿수 오류는 검증에 실패한다")
        void shouldFailWhenHyphenatedMiddleLengthInvalid(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "끝자리 자릿수 오류 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010-1234-567",
                "010-1234-56789"
        })
        @DisplayName("하이픈 포함 형식 끝자리 자릿수 오류는 검증에 실패한다")
        void shouldFailWhenHyphenatedSuffixLengthInvalid(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "하이픈 미포함 자릿수 오류 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "0101234",
                "010123456",
                "010123456789"
        })
        @DisplayName("하이픈 미포함 형식 자릿수 오류는 검증에 실패한다")
        void shouldFailWhenHyphenlessLengthInvalid(String value) {
            assertThat(isValid(value)).isFalse();
        }

        @ParameterizedTest(name = "숫자 외 문자 포함 {0} 은 검증에 실패한다")
        @ValueSource(strings = {
                "010-abcd-5678",
                "010.1234.5678",
                " 010-1234-5678"
        })
        @DisplayName("숫자/하이픈 외 문자를 포함한 형식은 검증에 실패한다")
        void shouldFailWhenContainsNonDigitOrHyphen(String value) {
            assertThat(isValid(value)).isFalse();
        }
    }
}
