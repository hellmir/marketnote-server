package com.personal.marketnote.common.adapter.in;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIdException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IdNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputFormatValidatorTest {

    @Nested
    @DisplayName("validateId(String)")
    class ValidateId {

        @ParameterizedTest
        @ValueSource(strings = {"1", "10", "100", "9999999999"})
        @DisplayName("양의 정수 문자열은 통과한다")
        void passesForPositiveInteger(String id) {
            assertThatCode(() -> InputFormatValidator.validateId(id))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        @DisplayName("null/empty/blank 입력은 IdNoValueException을 던진다")
        void throwsWhenBlank(String id) {
            assertThatThrownBy(() -> InputFormatValidator.validateId(id))
                    .isInstanceOf(IdNoValueException.class);
        }

        @Test
        @DisplayName("0은 InvalidIdException을 던진다")
        void throwsWhenZero() {
            assertThatThrownBy(() -> InputFormatValidator.validateId("0"))
                    .isInstanceOf(InvalidIdException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-100", "-9999"})
        @DisplayName("음수는 InvalidIdException을 던진다")
        void throwsWhenNegative(String id) {
            assertThatThrownBy(() -> InputFormatValidator.validateId(id))
                    .isInstanceOf(InvalidIdException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "12a", "a12", "1.5", "1,000"})
        @DisplayName("숫자가 아닌 문자가 포함되면 InvalidIdException을 던진다")
        void throwsWhenNotNumeric(String id) {
            assertThatThrownBy(() -> InputFormatValidator.validateId(id))
                    .isInstanceOf(InvalidIdException.class);
        }

        @Test
        @DisplayName("0으로 시작하는 숫자는 InvalidIdException을 던진다")
        void throwsWhenLeadingZero() {
            assertThatThrownBy(() -> InputFormatValidator.validateId("01"))
                    .isInstanceOf(InvalidIdException.class);
        }
    }
}
