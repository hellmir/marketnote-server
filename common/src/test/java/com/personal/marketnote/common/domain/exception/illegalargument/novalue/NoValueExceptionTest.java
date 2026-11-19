package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoValueException 테스트")
class NoValueExceptionTest {

    private static final class TestNoValueException extends NoValueException {
        TestNoValueException(String message) {
            super(message);
        }

        TestNoValueException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        NoValueException ex = new TestNoValueException("값이 없습니다");

        // then
        assertThat(ex.getMessage()).isEqualTo("값이 없습니다");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("upstream null");

        // when
        NoValueException ex = new TestNoValueException("값이 없습니다", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("값이 없습니다");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
