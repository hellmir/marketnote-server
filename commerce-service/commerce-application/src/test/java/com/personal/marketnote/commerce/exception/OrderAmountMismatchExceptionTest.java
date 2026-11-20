package com.personal.marketnote.commerce.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAmountMismatchExceptionTest {

    @Test
    @DisplayName("requestedTotal/calculatedTotal 단일 시그니처 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        OrderAmountMismatchException ex = new OrderAmountMismatchException(10000L, 9000L);

        assertThat(ex.getMessage()).contains("10000");
        assertThat(ex.getMessage()).contains("9000");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        ArithmeticException root = new ArithmeticException("long overflow");

        OrderAmountMismatchException ex = new OrderAmountMismatchException(10000L, -1L, root);

        assertThat(ex.getMessage()).contains("10000");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
