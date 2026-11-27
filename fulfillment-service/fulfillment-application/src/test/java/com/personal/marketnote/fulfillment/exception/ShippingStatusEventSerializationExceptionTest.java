package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingStatusEventSerializationExceptionTest {

    @Test
    @DisplayName("topic/partitionKey/eventId와 JsonProcessingException cause를 메시지+체인에 보존한다")
    void preservesContextAndCause() {
        JsonProcessingException root = new JsonProcessingException("serialize fail") {
        };

        ShippingStatusEventSerializationException ex = new ShippingStatusEventSerializationException(
            "shipping.status.changed", "999", "evt-1", root
        );

        assertThat(ex.getMessage())
            .contains("ERR_SHIPPING_STATUS_EVENT_01")
            .contains("배송 상태 변경 이벤트")
            .contains("topic=shipping.status.changed")
            .contains("partitionKey=999")
            .contains("eventId=evt-1");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
