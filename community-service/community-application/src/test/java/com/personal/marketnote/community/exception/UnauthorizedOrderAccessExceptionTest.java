package com.personal.marketnote.community.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnauthorizedOrderAccessExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        UnauthorizedOrderAccessException ex = new UnauthorizedOrderAccessException();

        assertThat(ex.getMessage()).contains("ERR_ORDER_AUTH_01");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("403 Forbidden");

        UnauthorizedOrderAccessException ex = new UnauthorizedOrderAccessException(root);

        assertThat(ex.getMessage()).contains("ERR_ORDER_AUTH_01");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
