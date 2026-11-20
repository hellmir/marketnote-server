package com.personal.marketnote.fulfillment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentGoodsAlreadyRegisteredExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 productKey를 메시지에 포함한다")
    void preservesMessageWithoutCause() {
        UUID productKey = UUID.randomUUID();

        FulfillmentGoodsAlreadyRegisteredException ex = new FulfillmentGoodsAlreadyRegisteredException(productKey);

        assertThat(ex.getMessage()).contains("이미 Fulfillment에 등록된 상품입니다.");
        assertThat(ex.getMessage()).contains(productKey.toString());
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        UUID productKey = UUID.randomUUID();
        RuntimeException root = new RuntimeException("constraint violation");

        FulfillmentGoodsAlreadyRegisteredException ex = new FulfillmentGoodsAlreadyRegisteredException(productKey, root);

        assertThat(ex.getMessage()).contains("이미 Fulfillment에 등록된 상품입니다.");
        assertThat(ex.getMessage()).contains(productKey.toString());
        assertThat(ex.getCause()).isSameAs(root);
    }
}
