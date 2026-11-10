package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.PHONE_NUMBER_INPUT_PATTERN;
import static com.personal.marketnote.common.utility.RegularExpressionConstant.PHONE_NUMBER_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberPatternTest {

    private final Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
    private final Pattern inputPattern = Pattern.compile(PHONE_NUMBER_INPUT_PATTERN);

    private boolean isValid(String value) {
        return FormatValidator.isValid(value, pattern);
    }

    private boolean isValidInput(String value) {
        return FormatValidator.isValid(value, inputPattern);
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

        @ParameterizedTest(name = "서울 유선 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "021234567",
                "0212345678"
        })
        @DisplayName("서울 유선(02) + canonical 9~10자리 형식은 검증에 성공한다")
        void shouldPassSeoulLandlineCanonical(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "광역/도 유선 prefix {0} 10~11자리 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "0311234567", "03112345678",
                "0321234567", "03212345678",
                "0331234567", "03312345678",
                "0411234567", "04112345678",
                "0421234567", "04212345678",
                "0431234567", "04312345678",
                "0441234567", "04412345678",
                "0511234567", "05112345678",
                "0521234567", "05212345678",
                "0531234567", "05312345678",
                "0541234567", "05412345678",
                "0551234567", "05512345678",
                "0611234567", "06112345678",
                "0621234567", "06212345678",
                "0631234567", "06312345678",
                "0641234567", "06412345678"
        })
        @DisplayName("광역/도 유선(031~064) + canonical 10~11자리 형식은 검증에 성공한다")
        void shouldPassRegionalLandlineCanonical(String value) {
            assertThat(isValid(value)).isTrue();
        }

        @ParameterizedTest(name = "인터넷 전화 {0} 형식은 검증에 성공한다")
        @ValueSource(strings = {
                "0701234567",
                "07012345678"
        })
        @DisplayName("인터넷 전화(070) + canonical 10~11자리 형식은 검증에 성공한다")
        void shouldPassInternetPhoneCanonical(String value) {
            assertThat(isValid(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("입력 패턴(하이픈 허용) 검증 성공")
    class 입력_패턴_검증_성공 {

        @ParameterizedTest(name = "하이픈 포함 입력 {0} 은 검증에 성공한다")
        @ValueSource(strings = {
                "010-1234-5678",
                "011-123-4567",
                "02-1234-5678",
                "02-123-4567",
                "031-123-4567",
                "031-1234-5678",
                "044-123-4567",
                "064-1234-5678",
                "070-1234-5678"
        })
        @DisplayName("허용 prefix + 하이픈 포함 입력 형식은 입력 패턴 검증에 성공한다")
        void shouldPassHyphenatedInputForAllowedPrefix(String value) {
            assertThat(isValidInput(value)).isTrue();
        }

        @ParameterizedTest(name = "canonical 입력 {0} 은 입력 패턴 검증에도 성공한다")
        @ValueSource(strings = {
                "01012345678",
                "0212345678",
                "021234567",
                "03112345678",
                "07012345678"
        })
        @DisplayName("허용 prefix + canonical 입력 형식은 입력 패턴 검증에 성공한다")
        void shouldPassCanonicalInputForAllowedPrefix(String value) {
            assertThat(isValidInput(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("입력 패턴(하이픈 허용) 검증 실패")
    class 입력_패턴_검증_실패 {

        @ParameterizedTest(name = "비정상 하이픈 입력 {0} 은 입력 패턴 검증에 실패한다")
        @ValueSource(strings = {
                "02--1234-5678",
                "02-1234--5678",
                "031--123-4567",
                "0212-345-6789",
                "031-12-34567",
                "070-12-345678"
        })
        @DisplayName("하이픈 위치/자릿수가 비정상인 입력은 입력 패턴 검증에 실패한다")
        void shouldFailWhenInputHyphenMalformed(String value) {
            assertThat(isValidInput(value)).isFalse();
        }

        @ParameterizedTest(name = "비허용 prefix 입력 {0} 은 입력 패턴 검증에 실패한다")
        @ValueSource(strings = {
                "015-123-4567",
                "065-123-4567",
                "080-1234-5678",
                "050-1234-5678"
        })
        @DisplayName("비허용 prefix 입력은 입력 패턴 검증에 실패한다")
        void shouldFailWhenInputPrefixInvalid(String value) {
            assertThat(isValidInput(value)).isFalse();
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
                "01212345678",
                "0151234567",
                "0301234567",
                "0341234567",
                "0401234567",
                "0451234567",
                "0501234567",
                "0561234567",
                "0601234567",
                "0651234567",
                "06512345678",
                "0801234567",
                "08012345678"
        })
        @DisplayName("허용되지 않는 prefix(015/030/034/040/045/050/056/060/065/080)는 검증에 실패한다")
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
