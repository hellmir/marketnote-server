package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentGoodsSyncedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존한다")
    void preservesCause() {
        JsonProcessingException root = new JsonProcessingException("goods synced serialize fail") {
        };

        FulfillmentGoodsSyncedEventSerializationException ex =
                new FulfillmentGoodsSyncedEventSerializationException(root);

        assertThat(ex.getMessage()).contains("풀필먼트 상품 동기화 이벤트 직렬화 실패");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
