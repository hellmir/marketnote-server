package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelFailedEventSerializationExceptionTest {

    @Test
    @DisplayName("topic/partitionKey/eventId와 JsonProcessingException cause를 메시지+체인에 보존한다")
    void preservesContextAndCause() {
        JsonProcessingException root = new JsonProcessingException("invalid token") {
        };

        OrderCancelFailedEventSerializationException ex = new OrderCancelFailedEventSerializationException(
            "order.cancel.failed", "12345", "evt-1", root
        );

        assertThat(ex.getMessage())
            .contains("ERR_ORDER_CANCEL_FAILED_EVENT_01")
            .contains("주문 취소 실패 이벤트")
            .contains("topic=order.cancel.failed")
            .contains("partitionKey=12345")
            .contains("eventId=evt-1");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
