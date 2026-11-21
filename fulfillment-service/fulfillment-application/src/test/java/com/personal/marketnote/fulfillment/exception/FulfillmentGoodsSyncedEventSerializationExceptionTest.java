package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentGoodsSyncedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존하고 메시지에 컨텍스트가 포함된다")
    void preservesCauseAndContext() {
        JsonProcessingException root = new JsonProcessingException("goods synced serialize fail") {
        };

        FulfillmentGoodsSyncedEventSerializationException ex =
                new FulfillmentGoodsSyncedEventSerializationException(
                        "fulfillment.goods.synced", "GOODS-001", "evt-1", root
                );

        assertThat(ex.getMessage())
                .contains("ERR_FULFILLMENT_GOODS_SYNCED_EVENT_01")
                .contains("풀필먼트 상품 동기화 이벤트")
                .contains("topic=fulfillment.goods.synced")
                .contains("partitionKey=GOODS-001")
                .contains("eventId=evt-1");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
