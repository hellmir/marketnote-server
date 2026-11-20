package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelFailedEventSerializationExceptionTest {

    @Test
    @DisplayName("orderId와 JsonProcessingException cause를 메시지+체인에 보존한다")
    void preservesOrderIdAndCause() {
        Long orderId = 12345L;
        JsonProcessingException root = new JsonProcessingException("invalid token") {
        };

        OrderCancelFailedEventSerializationException ex = new OrderCancelFailedEventSerializationException(orderId, root);

        assertThat(ex.getMessage()).contains("주문 취소 실패 이벤트 직렬화 실패");
        assertThat(ex.getMessage()).contains(String.valueOf(orderId));
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
