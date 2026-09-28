package com.personal.marketnote.common.utility;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIdException;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.ParsingBooleanException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingByteException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingDoubleException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingFloatException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingIntegerException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingLongException;
import com.personal.marketnote.common.domain.exception.illegalargument.numberformat.ParsingShortException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatConverterTest {

    @Nested
    @DisplayName("parseId(String)")
    class ParseId {

        @Test
        @DisplayName("정상 숫자 문자열은 Long으로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseId("123")).isEqualTo(123L);
        }

        @Test
        @DisplayName("음수도 파싱한다")
        void parsesNegativeNumber() {
            assertThat(FormatConverter.parseId("-1")).isEqualTo(-1L);
        }

        @Test
        @DisplayName("숫자가 아니면 InvalidIdException을 던진다")
        void throwsInvalidIdExceptionWhenNotNumber() {
            assertThatThrownBy(() -> FormatConverter.parseId("abc"))
                    .isInstanceOf(InvalidIdException.class);
        }
    }

    @Nested
    @DisplayName("parseToLong(String)")
    class ParseToLong {

        @Test
        @DisplayName("정상 숫자 문자열은 Long으로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToLong("9999999999")).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("숫자가 아니면 ParsingLongException을 던진다")
        void throwsParsingLongException() {
            assertThatThrownBy(() -> FormatConverter.parseToLong("abc"))
                    .isInstanceOf(ParsingLongException.class);
        }
    }

    @Nested
    @DisplayName("parseToInteger(String)")
    class ParseToInteger {

        @Test
        @DisplayName("정상 숫자 문자열은 Integer로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToInteger("123")).isEqualTo(123);
        }

        @Test
        @DisplayName("Integer 범위 초과 시 ParsingIntegerException을 던진다")
        void throwsWhenOverflow() {
            assertThatThrownBy(() -> FormatConverter.parseToInteger("9999999999"))
                    .isInstanceOf(ParsingIntegerException.class);
        }

        @Test
        @DisplayName("숫자가 아니면 ParsingIntegerException을 던진다")
        void throwsWhenNotNumber() {
            assertThatThrownBy(() -> FormatConverter.parseToInteger("xyz"))
                    .isInstanceOf(ParsingIntegerException.class);
        }
    }

    @Nested
    @DisplayName("parseToShort(String)")
    class ParseToShort {

        @Test
        @DisplayName("정상 숫자 문자열은 Short로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToShort("123")).isEqualTo((short) 123);
        }

        @Test
        @DisplayName("Short 범위 초과 시 ParsingShortException을 던진다")
        void throwsWhenOverflow() {
            assertThatThrownBy(() -> FormatConverter.parseToShort("99999"))
                    .isInstanceOf(ParsingShortException.class);
        }
    }

    @Nested
    @DisplayName("parseToByte(String)")
    class ParseToByte {

        @Test
        @DisplayName("정상 숫자 문자열은 Byte로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToByte("100")).isEqualTo((byte) 100);
        }

        @Test
        @DisplayName("Byte 범위 초과 시 ParsingByteException을 던진다")
        void throwsWhenOverflow() {
            assertThatThrownBy(() -> FormatConverter.parseToByte("999"))
                    .isInstanceOf(ParsingByteException.class);
        }
    }

    @Nested
    @DisplayName("parseToDouble(String)")
    class ParseToDouble {

        @Test
        @DisplayName("정상 실수 문자열은 Double로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToDouble("3.14")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("숫자가 아니면 ParsingDoubleException을 던진다")
        void throwsWhenNotNumber() {
            assertThatThrownBy(() -> FormatConverter.parseToDouble("abc"))
                    .isInstanceOf(ParsingDoubleException.class);
        }
    }

    @Nested
    @DisplayName("parseToFloat(String)")
    class ParseToFloat {

        @Test
        @DisplayName("정상 실수 문자열은 Float로 파싱한다")
        void parsesValidNumber() {
            assertThat(FormatConverter.parseToFloat("3.14")).isEqualTo(3.14f);
        }

        @Test
        @DisplayName("숫자가 아니면 ParsingFloatException을 던진다")
        void throwsWhenNotNumber() {
            assertThatThrownBy(() -> FormatConverter.parseToFloat("xyz"))
                    .isInstanceOf(ParsingFloatException.class);
        }
    }

    @Nested
    @DisplayName("parseToBoolean(String)")
    class ParseToBoolean {

        @Test
        @DisplayName("문자열 'true'는 true를 반환한다")
        void parsesTrue() {
            assertThat(FormatConverter.parseToBoolean("true")).isTrue();
        }

        @Test
        @DisplayName("문자열 'false'는 false를 반환한다")
        void parsesFalse() {
            assertThat(FormatConverter.parseToBoolean("false")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"True", "TRUE", "yes", "1", "abc"})
        @DisplayName("'true'/'false'가 아니면 ParsingBooleanException을 던진다")
        void throwsWhenInvalid(String value) {
            assertThatThrownBy(() -> FormatConverter.parseToBoolean(value))
                    .isInstanceOf(ParsingBooleanException.class);
        }

        @Test
        @DisplayName("null 입력 시 NullPointerException이 발생한다 (현재 동작 고정)")
        void throwsNpeWhenNull() {
            assertThatThrownBy(() -> FormatConverter.parseToBoolean(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toUpperCase(String) / toLowerCase(String)")
    class ChangeCase {

        @Test
        @DisplayName("toUpperCase는 영문자를 대문자로 변환한다")
        void convertsToUpper() {
            assertThat(FormatConverter.toUpperCase("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("toLowerCase는 영문자를 소문자로 변환한다")
        void convertsToLower() {
            assertThat(FormatConverter.toLowerCase("HELLO")).isEqualTo("hello");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("toUpperCase는 null/empty/blank 입력을 그대로 반환한다")
        void returnsAsIsForBlankUpper(String value) {
            assertThat(FormatConverter.toUpperCase(value)).isEqualTo(value);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("toLowerCase는 null/empty/blank 입력을 그대로 반환한다")
        void returnsAsIsForBlankLower(String value) {
            assertThat(FormatConverter.toLowerCase(value)).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("snakeToCamel(String)")
    class SnakeToCamel {

        @Test
        @DisplayName("snake_case를 camelCase로 변환한다")
        void convertsSnakeToCamel() {
            assertThat(FormatConverter.snakeToCamel("hello_world")).isEqualTo("helloWorld");
        }

        @Test
        @DisplayName("연속된 단어도 변환한다")
        void convertsMultipleWords() {
            assertThat(FormatConverter.snakeToCamel("user_name_first")).isEqualTo("userNameFirst");
        }

        @Test
        @DisplayName("대문자도 소문자로 변환된다")
        void normalizesUpperToLower() {
            assertThat(FormatConverter.snakeToCamel("USER_NAME")).isEqualTo("userName");
        }

        @Test
        @DisplayName("언더스코어가 없으면 모두 소문자로 변환한다")
        void noUnderscore() {
            assertThat(FormatConverter.snakeToCamel("Hello")).isEqualTo("hello");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("null/empty/blank 입력은 빈 문자열을 반환한다")
        void returnsEmptyForBlank(String value) {
            assertThat(FormatConverter.snakeToCamel(value)).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("sanitizeFileName(String)")
    class SanitizeFileName {

        @Test
        @DisplayName("공백을 하이픈으로 바꾼다")
        void replacesWhitespaceWithHyphen() {
            assertThat(FormatConverter.sanitizeFileName("hello world")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("연속된 공백도 하나의 하이픈으로 바꾼다")
        void replacesMultipleWhitespace() {
            assertThat(FormatConverter.sanitizeFileName("hello   world")).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("허용되지 않는 특수문자는 제거한다")
        void removesSpecialCharacters() {
            assertThat(FormatConverter.sanitizeFileName("file@name#1!.txt")).isEqualTo("filename1.txt");
        }

        @Test
        @DisplayName("영문/숫자/점/언더스코어/하이픈은 유지한다")
        void keepsAllowedCharacters() {
            assertThat(FormatConverter.sanitizeFileName("file_name-1.0.txt")).isEqualTo("file_name-1.0.txt");
        }

        @Test
        @DisplayName("한글은 제거된다")
        void removesKorean() {
            assertThat(FormatConverter.sanitizeFileName("파일.txt")).isEqualTo(".txt");
        }
    }

    @Nested
    @DisplayName("parseToNumberTime(LocalDateTime)")
    class ParseToNumberTime {

        @Test
        @DisplayName("LocalDateTime을 yyyyMMddHHmmss 형식으로 변환한다")
        void formatsLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2026, 4, 14, 9, 30, 45);
            assertThat(FormatConverter.parseToNumberTime(dateTime)).isEqualTo("20260414093045");
        }

        @Test
        @DisplayName("한 자리 시/분/초는 0으로 패딩한다")
        void padsSingleDigits() {
            LocalDateTime dateTime = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
            assertThat(FormatConverter.parseToNumberTime(dateTime)).isEqualTo("20260101000000");
        }
    }

    @Nested
    @DisplayName("parseToLocalDateTime(String)")
    class ParseToLocalDateTime {

        @Test
        @DisplayName("ISO 형식 문자열을 LocalDateTime으로 파싱한다")
        void parsesIsoDateTime() {
            LocalDateTime result = FormatConverter.parseToLocalDateTime("2026-09-28T09:30:45");
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 14, 9, 30, 45));
        }

        @Test
        @DisplayName("yyyy-MM-dd HH:mm:ss 형식 문자열을 LocalDateTime으로 파싱한다")
        void parsesSpaceFormat() {
            LocalDateTime result = FormatConverter.parseToLocalDateTime("2026-09-28 09:30:45");
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 14, 9, 30, 45));
        }

        @Test
        @DisplayName("epoch seconds 문자열을 Asia/Seoul 기준 LocalDateTime으로 파싱한다")
        void parsesEpochSeconds() {
            long epochSeconds = ZonedDateTime.of(2026, 4, 14, 18, 30, 45, 0, ZoneId.of("Asia/Seoul"))
                    .toEpochSecond();
            LocalDateTime result = FormatConverter.parseToLocalDateTime(String.valueOf(epochSeconds));
            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 14, 18, 30, 45));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("null/empty/blank 입력은 null을 반환한다")
        void returnsNullForBlank(String value) {
            assertThat(FormatConverter.parseToLocalDateTime(value)).isNull();
        }

        @Test
        @DisplayName("어떤 포맷에도 매칭되지 않으면 null을 반환한다")
        void returnsNullForInvalidFormat() {
            assertThat(FormatConverter.parseToLocalDateTime("invalid-date")).isNull();
        }
    }
}
