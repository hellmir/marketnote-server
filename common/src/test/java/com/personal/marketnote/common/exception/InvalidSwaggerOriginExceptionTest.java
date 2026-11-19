package com.personal.marketnote.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidSwaggerOriginExceptionTest {

    @Test
    @DisplayName("origin 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        InvalidSwaggerOriginException ex = new InvalidSwaggerOriginException("badOrigin");

        assertThat(ex.getMessage()).contains("badOrigin");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("origin과 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        IllegalArgumentException root = new IllegalArgumentException("URI parse failed");

        InvalidSwaggerOriginException ex = new InvalidSwaggerOriginException("badOrigin", root);

        assertThat(ex.getMessage()).contains("badOrigin");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
