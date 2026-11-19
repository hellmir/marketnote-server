package com.personal.marketnote.user.service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnlinkOauth2AccountFailedExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 메시지를 보존하고 cause 는 null 이다")
    void shouldKeepMessageAndNullCauseForMessageConstructor() {
        // when
        UnlinkOauth2AccountFailedException exception = new UnlinkOauth2AccountFailedException("연결 해제 실패");

        // then
        assertThat(exception.getMessage()).isEqualTo("연결 해제 실패");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Throwable cause 생성자는 메시지를 보존하고 원인 예외를 cause 로 전파한다")
    void shouldPreserveCauseWhenConstructedWithThrowable() {
        // given
        RuntimeException rootCause = new RuntimeException("KAKAO unlink 4xx");

        // when
        UnlinkOauth2AccountFailedException exception = new UnlinkOauth2AccountFailedException("연결 해제 실패", rootCause);

        // then
        assertThat(exception.getMessage()).isEqualTo("연결 해제 실패");
        assertThat(exception.getCause()).isSameAs(rootCause);
    }
}
