package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentDeliveryWorkStatusChangedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존한다")
    void preservesCause() {
        JsonProcessingException root = new JsonProcessingException("work status serialize fail") {
        };

        FulfillmentDeliveryWorkStatusChangedEventSerializationException ex =
                new FulfillmentDeliveryWorkStatusChangedEventSerializationException(root);

        assertThat(ex.getMessage()).contains("풀필먼트 배송 작업 상태 변경 이벤트 직렬화 실패");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
