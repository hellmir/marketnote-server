package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatValidatorTest {

    @Nested
    @DisplayName("isValid(CharSequence, Pattern)")
    class IsValid {

        private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        @Test
        @DisplayName("값과 패턴이 매칭되면 true를 반환한다")
        void returnsTrueWhenMatches() {
            assertThat(FormatValidator.isValid("user@example.com", emailPattern)).isTrue();
        }

        @Test
        @DisplayName("값이 패턴과 매칭되지 않으면 false를 반환한다")
        void returnsFalseWhenNotMatches() {
            assertThat(FormatValidator.isValid("invalid-email", emailPattern)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("값이 null이거나 빈 값/공백이면 false를 반환한다")
        void returnsFalseWhenValueIsBlank(String value) {
            assertThat(FormatValidator.isValid(value, emailPattern)).isFalse();
        }

        @Test
        @DisplayName("패턴이 null이면 false를 반환한다")
        void returnsFalseWhenPatternIsNull() {
            assertThat(FormatValidator.isValid("user@example.com", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasValue/hasNoValue(Object)")
    class HasValueObject {

        @Test
        @DisplayName("값이 있으면 hasValue=true, hasNoValue=false")
        void returnsTrueWhenHasValue() {
            assertThat(FormatValidator.hasValue("text")).isTrue();
            assertThat(FormatValidator.hasNoValue("text")).isFalse();
        }

        @Test
        @DisplayName("값이 null이면 hasValue=false, hasNoValue=true")
        void returnsFalseWhenNull() {
            Object value = null;
            assertThat(FormatValidator.hasValue(value)).isFalse();
            assertThat(FormatValidator.hasNoValue(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        @DisplayName("값이 빈 문자열이거나 공백/탭/개행이면 hasValue=false")
        void returnsFalseWhenBlank(String value) {
            assertThat(FormatValidator.hasValue(value)).isFalse();
            assertThat(FormatValidator.hasNoValue(value)).isTrue();
        }

        @Test
        @DisplayName("toString이 비어있지 않은 임의 Object는 hasValue=true")
        void returnsTrueForNonBlankObject() {
            Object value = new Object() {
                @Override
                public String toString() {
                    return "non-blank";
                }
            };
            assertThat(FormatValidator.hasValue(value)).isTrue();
            assertThat(FormatValidator.hasNoValue(value)).isFalse();
        }

        @Test
        @DisplayName("toString이 빈 문자열인 임의 Object는 hasValue=false")
        void returnsFalseForBlankObject() {
            Object value = new Object() {
                @Override
                public String toString() {
                    return "";
                }
            };
            assertThat(FormatValidator.hasValue(value)).isFalse();
            assertThat(FormatValidator.hasNoValue(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("hasValue/hasNoValue(Collection)")
    class HasValueCollection {

        @Test
        @DisplayName("컬렉션이 null이면 hasValue=false, hasNoValue=true")
        void returnsFalseWhenNull() {
            Collection<String> collection = null;
            assertThat(FormatValidator.hasValue(collection)).isFalse();
            assertThat(FormatValidator.hasNoValue(collection)).isTrue();
        }

        @Test
        @DisplayName("컬렉션이 비어있으면 hasValue=false, hasNoValue=true")
        void returnsFalseWhenEmpty() {
            assertThat(FormatValidator.hasValue(Collections.emptyList())).isFalse();
            assertThat(FormatValidator.hasNoValue(Collections.emptyList())).isTrue();
        }

        @Test
        @DisplayName("컬렉션에 값이 있으면 hasValue=true, hasNoValue=false")
        void returnsTrueWhenNotEmpty() {
            List<String> list = List.of("item");
            assertThat(FormatValidator.hasValue(list)).isTrue();
            assertThat(FormatValidator.hasNoValue(list)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasValue/hasNoValue(Number)")
    class HasValueNumber {

        @Test
        @DisplayName("Number가 null이면 hasValue=false, hasNoValue=true")
        void returnsFalseWhenNull() {
            Number number = null;
            assertThat(FormatValidator.hasValue(number)).isFalse();
            assertThat(FormatValidator.hasNoValue(number)).isTrue();
        }

        @Test
        @DisplayName("Number가 0이어도 hasValue=true (null 체크만)")
        void returnsTrueWhenZero() {
            assertThat(FormatValidator.hasValue(Integer.valueOf(0))).isTrue();
            assertThat(FormatValidator.hasNoValue(Integer.valueOf(0))).isFalse();
        }

        @Test
        @DisplayName("음수도 hasValue=true")
        void returnsTrueWhenNegative() {
            assertThat(FormatValidator.hasValue(Long.valueOf(-1L))).isTrue();
            assertThat(FormatValidator.hasNoValue(Long.valueOf(-1L))).isFalse();
        }

        @Test
        @DisplayName("양수도 hasValue=true")
        void returnsTrueWhenPositive() {
            assertThat(FormatValidator.hasValue(Double.valueOf(3.14))).isTrue();
            assertThat(FormatValidator.hasNoValue(Double.valueOf(3.14))).isFalse();
        }
    }

    @Nested
    @DisplayName("equals/notEquals(Object, Object)")
    class Equals {

        @Test
        @DisplayName("두 값이 같으면 equals=true, notEquals=false")
        void returnsTrueWhenEqual() {
            assertThat(FormatValidator.equals("abc", "abc")).isTrue();
            assertThat(FormatValidator.notEquals("abc", "abc")).isFalse();
        }

        @Test
        @DisplayName("두 값이 다르면 equals=false, notEquals=true")
        void returnsFalseWhenDifferent() {
            assertThat(FormatValidator.equals("abc", "xyz")).isFalse();
            assertThat(FormatValidator.notEquals("abc", "xyz")).isTrue();
        }

        @Test
        @DisplayName("한쪽이 null이면 equals=false")
        void returnsFalseWhenOneIsNull() {
            assertThat(FormatValidator.equals(null, "abc")).isFalse();
            assertThat(FormatValidator.equals("abc", null)).isFalse();
            assertThat(FormatValidator.notEquals(null, "abc")).isTrue();
        }

        @Test
        @DisplayName("두 값 모두 null이면 equals=false")
        void returnsFalseWhenBothNull() {
            assertThat(FormatValidator.equals(null, null)).isFalse();
            assertThat(FormatValidator.notEquals(null, null)).isTrue();
        }

        @Test
        @DisplayName("한쪽이 빈 문자열이면 equals=false")
        void returnsFalseWhenOneIsBlank() {
            assertThat(FormatValidator.equals("", "abc")).isFalse();
            assertThat(FormatValidator.equals("abc", "")).isFalse();
        }
    }

    @Nested
    @DisplayName("equalsIgnoreCase/notEqualsIgnoreCase")
    class EqualsIgnoreCase {

        @Test
        @DisplayName("대소문자가 다른 같은 값이면 equalsIgnoreCase=true")
        void returnsTrueWhenEqualIgnoringCase() {
            assertThat(FormatValidator.equalsIgnoreCase("ABC", "abc")).isTrue();
            assertThat(FormatValidator.notEqualsIgnoreCase("ABC", "abc")).isFalse();
        }

        @Test
        @DisplayName("값이 다르면 equalsIgnoreCase=false")
        void returnsFalseWhenDifferent() {
            assertThat(FormatValidator.equalsIgnoreCase("ABC", "XYZ")).isFalse();
            assertThat(FormatValidator.notEqualsIgnoreCase("ABC", "XYZ")).isTrue();
        }

        @Test
        @DisplayName("한쪽이 null이면 equalsIgnoreCase=false")
        void returnsFalseWhenOneIsNull() {
            assertThat(FormatValidator.equalsIgnoreCase(null, "abc")).isFalse();
            assertThat(FormatValidator.equalsIgnoreCase("abc", null)).isFalse();
            assertThat(FormatValidator.notEqualsIgnoreCase(null, "abc")).isTrue();
        }

        @Test
        @DisplayName("두 값 모두 null이면 equalsIgnoreCase=false")
        void returnsFalseWhenBothNull() {
            assertThat(FormatValidator.equalsIgnoreCase(null, null)).isFalse();
            assertThat(FormatValidator.notEqualsIgnoreCase(null, null)).isTrue();
        }

        @Test
        @DisplayName("한쪽이 빈 문자열이면 equalsIgnoreCase=false")
        void returnsFalseWhenOneIsBlank() {
            assertThat(FormatValidator.equalsIgnoreCase("", "abc")).isFalse();
            assertThat(FormatValidator.equalsIgnoreCase("abc", "")).isFalse();
        }
    }

    @Nested
    @DisplayName("containsKeyword(String, String)")
    class ContainsKeyword {

        @Test
        @DisplayName("대상에 키워드가 포함되면 true를 반환한다")
        void returnsTrueWhenContains() {
            assertThat(FormatValidator.containsKeyword("Hello World", "world")).isTrue();
        }

        @Test
        @DisplayName("대소문자 무시하고 포함 여부를 판단한다")
        void returnsTrueIgnoringCase() {
            assertThat(FormatValidator.containsKeyword("Hello World", "WORLD")).isTrue();
            assertThat(FormatValidator.containsKeyword("HELLO WORLD", "hello")).isTrue();
        }

        @Test
        @DisplayName("키워드가 포함되지 않으면 false를 반환한다")
        void returnsFalseWhenNotContains() {
            assertThat(FormatValidator.containsKeyword("Hello World", "java")).isFalse();
        }

        @Test
        @DisplayName("빈 키워드는 항상 true를 반환한다")
        void returnsTrueWhenKeywordIsEmpty() {
            assertThat(FormatValidator.containsKeyword("Hello", "")).isTrue();
        }

        @Test
        @DisplayName("target이 null이면 NullPointerException이 발생한다")
        void throwsNpeWhenTargetIsNull() {
            assertThatThrownBy(() -> FormatValidator.containsKeyword(null, "x"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("keyword가 null이면 NullPointerException이 발생한다")
        void throwsNpeWhenKeywordIsNull() {
            assertThatThrownBy(() -> FormatValidator.containsKeyword("Hello", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
