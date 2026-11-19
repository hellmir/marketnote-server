package com.personal.marketnote.notification.domain.device;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPlatformExceptionTest {

    @Test
    @DisplayName("메시지 생성자는 ERR_DEVICE_TOKEN_01 에러 코드를 포함하고 cause 가 없다")
    void shouldContainErrorCodeWithoutCause() {
        // when
        InvalidPlatformException exception = new InvalidPlatformException("플랫폼 값이 비어있습니다.");

        // then
        assertThat(exception.getMessage()).startsWith("ERR_DEVICE_TOKEN_01::");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 시그니처는 원인 예외를 보존한다")
    void shouldPreserveCauseWhenProvided() {
        // given
        Throwable cause = new IllegalArgumentException("No enum constant");

        // when
        InvalidPlatformException exception = new InvalidPlatformException("지원하지 않는 플랫폼입니다.", cause);

        // then
        assertThat(exception.getMessage()).startsWith("ERR_DEVICE_TOKEN_01::");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
