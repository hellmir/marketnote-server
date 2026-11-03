package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberNormalizerTest {

    @Nested
    @DisplayName("normalize(String)")
    class Normalize {

        @ParameterizedTest(name = "하이픈 포함 {0} 은 canonical {1} 로 정규화된다")
        @CsvSource({
                "010-1234-5678, 01012345678",
                "011-1234-5678, 01112345678",
                "010-123-4567, 0101234567",
                "011-123-4567, 0111234567"
        })
        @DisplayName("하이픈 포함 형식은 하이픈이 제거된 canonical 형식으로 정규화된다")
        void shouldRemoveHyphensFromHyphenatedFormat(String raw, String expected) {
            assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "하이픈 미포함 {0} 은 그대로 반환된다")
        @ValueSource(strings = {"01012345678", "0111234567", "01612345678"})
        @DisplayName("하이픈이 없는 canonical 형식은 원문 그대로 반환된다")
        void shouldReturnAsIsWhenAlreadyCanonical(String raw) {
            assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(raw);
        }

        @ParameterizedTest(name = "하이픈 혼용 {0} 은 모두 제거되어 canonical 로 정규화된다")
        @CsvSource({
                "010-12345678, 01012345678",
                "0101234-5678, 01012345678"
        })
        @DisplayName("하이픈 혼용 형식도 모든 하이픈이 제거된 canonical 형식으로 정규화된다")
        void shouldRemoveAllHyphensFromMixedFormat(String raw, String expected) {
            assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 문자열은 원문 그대로 반환한다")
        void shouldReturnAsIsWhenNullOrEmpty(String raw) {
            assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(raw);
        }

        @Test
        @DisplayName("공백만 있는 문자열은 원문 그대로 반환한다")
        void shouldReturnAsIsWhenBlank() {
            String raw = "   ";

            assertThat(PhoneNumberNormalizer.normalize(raw)).isEqualTo(raw);
        }
    }
}
