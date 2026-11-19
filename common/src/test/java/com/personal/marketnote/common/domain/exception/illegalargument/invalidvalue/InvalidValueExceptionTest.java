package com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidValueException 테스트")
class InvalidValueExceptionTest {

    private static final class TestInvalidValueException extends InvalidValueException {
        TestInvalidValueException(String message) {
            super(message);
        }

        TestInvalidValueException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        InvalidValueException ex = new TestInvalidValueException("유효하지 않은 값");

        // then
        assertThat(ex.getMessage()).isEqualTo("유효하지 않은 값");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new NumberFormatException("not a number");

        // when
        InvalidValueException ex = new TestInvalidValueException("유효하지 않은 값", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("유효하지 않은 값");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
