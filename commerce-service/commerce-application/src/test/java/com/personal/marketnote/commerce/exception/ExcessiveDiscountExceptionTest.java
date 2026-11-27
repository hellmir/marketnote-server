package com.personal.marketnote.commerce.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExcessiveDiscountExceptionTest {

    @Test
    @DisplayName("4-인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        ExcessiveDiscountException ex = new ExcessiveDiscountException(10000L, 3000L, 5000L, 2000L);

        assertThat(ex.getMessage()).contains("10000");
        assertThat(ex.getMessage()).contains("5000");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 5-인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        ArithmeticException root = new ArithmeticException("long overflow");

        ExcessiveDiscountException ex =
            new ExcessiveDiscountException(10000L, 3000L, 5000L, 2000L, root);

        assertThat(ex.getMessage()).contains("10000");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
