package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidBrandTalkMessageExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 ERR_BRAND_TALK_MESSAGE 에러 코드를 포함하고 cause 가 없다")
    void shouldContainErrorCodeWithoutCause() {
        // when
        InvalidBrandTalkMessageException exception = new InvalidBrandTalkMessageException("수신자 변수 직렬화 실패");

        // then
        assertThat(exception.getMessage()).startsWith("ERR_BRAND_TALK_MESSAGE::");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 시그니처는 원인 예외를 보존한다")
    void shouldPreserveCauseWhenProvided() {
        // given
        Throwable cause = new RuntimeException("Json 직렬화 실패");

        // when
        InvalidBrandTalkMessageException exception = new InvalidBrandTalkMessageException("수신자 변수 직렬화 실패", cause);

        // then
        assertThat(exception.getMessage()).startsWith("ERR_BRAND_TALK_MESSAGE::");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
