package com.personal.marketnote.fulfillment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentDeliveryAlreadyRegisteredExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        FulfillmentDeliveryAlreadyRegisteredException ex = new FulfillmentDeliveryAlreadyRegisteredException();

        assertThat(ex.getMessage()).contains("이미 Fulfillment에 출고 요청된 주문입니다.");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("constraint violation");

        FulfillmentDeliveryAlreadyRegisteredException ex = new FulfillmentDeliveryAlreadyRegisteredException(root);

        assertThat(ex.getMessage()).contains("이미 Fulfillment에 출고 요청된 주문입니다.");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
