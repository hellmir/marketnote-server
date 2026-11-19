package com.personal.marketnote.user.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteAreaAlreadyExistsExceptionTest {

    @Test
    @DisplayName("기본 생성자는 메시지를 보존하고 cause 는 null 이다")
    void shouldKeepMessageAndNullCauseForDefaultConstructor() {
        // when
        RemoteAreaAlreadyExistsException exception = new RemoteAreaAlreadyExistsException();

        // then
        assertThat(exception.getMessage()).contains("이미 등록된 도서산간 지역입니다");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Throwable cause 생성자는 메시지를 보존하고 원인 예외를 cause 로 전파한다")
    void shouldPreserveCauseWhenConstructedWithThrowable() {
        // given
        Throwable rootCause = new IllegalStateException("DB unique constraint violation");

        // when
        RemoteAreaAlreadyExistsException exception = new RemoteAreaAlreadyExistsException(rootCause);

        // then
        assertThat(exception.getMessage()).contains("이미 등록된 도서산간 지역입니다");
        assertThat(exception.getCause()).isSameAs(rootCause);
    }
}
