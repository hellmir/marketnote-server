package com.personal.marketnote.common.domain.exception.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthenticationFailedException 테스트")
class AuthenticationFailedExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessage_withoutCause() {
        // when
        AuthenticationFailedException ex = new AuthenticationFailedException("인증 실패");

        // then
        assertThat(ex.getMessage()).isEqualTo("인증 실패");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause 를 보존한다")
    void preservesCause() {
        // given
        Throwable cause = new IllegalStateException("토큰 만료");

        // when
        AuthenticationFailedException ex = new AuthenticationFailedException("인증 실패", cause);

        // then
        assertThat(ex.getMessage()).isEqualTo("인증 실패");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
