package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentInventorySyncedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존한다")
    void preservesCause() {
        JsonProcessingException root = new JsonProcessingException("inventory serialize fail") {
        };

        FulfillmentInventorySyncedEventSerializationException ex =
                new FulfillmentInventorySyncedEventSerializationException(root);

        assertThat(ex.getMessage()).contains("풀필먼트 재고 동기화 이벤트 직렬화 실패");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
