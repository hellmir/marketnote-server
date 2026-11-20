package com.personal.marketnote.product.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingPolicyAlreadyExistsExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        ShippingPolicyAlreadyExistsException ex = new ShippingPolicyAlreadyExistsException();

        assertThat(ex.getMessage()).contains("배송비 정책");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("DataIntegrityViolation");

        ShippingPolicyAlreadyExistsException ex = new ShippingPolicyAlreadyExistsException(root);

        assertThat(ex.getMessage()).contains("배송비 정책");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
