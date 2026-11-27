package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentDeliveryWorkStatusChangedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존하고 메시지에 컨텍스트가 포함된다")
    void preservesCauseAndContext() {
        JsonProcessingException root = new JsonProcessingException("work status serialize fail") {
        };

        FulfillmentDeliveryWorkStatusChangedEventSerializationException ex =
            new FulfillmentDeliveryWorkStatusChangedEventSerializationException(
                "fulfillment.delivery.work.status.changed", "1234", "evt-1", root
            );

        assertThat(ex.getMessage())
            .contains("ERR_FULFILLMENT_DELIVERY_WORK_STATUS_EVENT_01")
            .contains("풀필먼트 배송 작업 상태 변경 이벤트")
            .contains("topic=fulfillment.delivery.work.status.changed")
            .contains("partitionKey=1234")
            .contains("eventId=evt-1");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
