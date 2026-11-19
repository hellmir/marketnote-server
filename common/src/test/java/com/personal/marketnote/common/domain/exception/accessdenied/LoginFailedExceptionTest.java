package com.personal.marketnote.common.domain.exception.accessdenied;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginFailedException 테스트")
class LoginFailedExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        LoginFailedException ex = new LoginFailedException("로그인 실패");

        // then
        assertThat(ex.getMessage()).isEqualTo("로그인 실패");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("자격 증명 누락");

        // when
        LoginFailedException ex = new LoginFailedException("로그인 실패", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("로그인 실패");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
