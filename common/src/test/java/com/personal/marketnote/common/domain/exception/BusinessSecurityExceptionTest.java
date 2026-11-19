package com.personal.marketnote.common.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessSecurityException 테스트")
class BusinessSecurityExceptionTest {

    private static final class TestSecurityException extends BusinessSecurityException {
        TestSecurityException(String message) {
            super(message);
        }

        TestSecurityException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        BusinessSecurityException ex = new TestSecurityException("인가 실패");

        // then
        assertThat(ex.getMessage()).isEqualTo("인가 실패");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("토큰 만료");

        // when
        BusinessSecurityException ex = new TestSecurityException("인가 실패", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("인가 실패");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
