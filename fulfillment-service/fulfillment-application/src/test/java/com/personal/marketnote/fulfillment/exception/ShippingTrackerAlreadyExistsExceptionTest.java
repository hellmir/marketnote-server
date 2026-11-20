package com.personal.marketnote.fulfillment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingTrackerAlreadyExistsExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        ShippingTrackerAlreadyExistsException ex = new ShippingTrackerAlreadyExistsException();

        assertThat(ex.getMessage()).contains("이미 배송 추적이 등록된 주문입니다.");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("constraint violation");

        ShippingTrackerAlreadyExistsException ex = new ShippingTrackerAlreadyExistsException(root);

        assertThat(ex.getMessage()).contains("이미 배송 추적이 등록된 주문입니다.");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
