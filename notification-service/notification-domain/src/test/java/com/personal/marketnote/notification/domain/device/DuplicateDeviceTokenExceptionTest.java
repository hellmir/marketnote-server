package com.personal.marketnote.notification.domain.device;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateDeviceTokenExceptionTest {

    @Test
    @DisplayName("기본 생성자는 ERR_DEVICE_TOKEN_04 에러 코드를 포함하고 cause 가 없다")
    void shouldContainErrorCodeWithoutCause() {
        // when
        DuplicateDeviceTokenException exception = new DuplicateDeviceTokenException();

        // then
        assertThat(exception.getMessage()).startsWith("ERR_DEVICE_TOKEN_04::");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 시그니처는 원인 예외를 보존한다")
    void shouldPreserveCauseWhenProvided() {
        // given
        Throwable cause = new RuntimeException("DB unique 제약 위반");

        // when
        DuplicateDeviceTokenException exception = new DuplicateDeviceTokenException(cause);

        // then
        assertThat(exception.getMessage()).startsWith("ERR_DEVICE_TOKEN_04::");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
