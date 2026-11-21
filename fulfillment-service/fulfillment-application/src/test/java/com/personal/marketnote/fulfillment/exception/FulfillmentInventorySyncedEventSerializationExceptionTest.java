package com.personal.marketnote.fulfillment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentInventorySyncedEventSerializationExceptionTest {

    @Test
    @DisplayName("JsonProcessingException cause를 체인에 보존하고 메시지에 컨텍스트가 포함된다")
    void preservesCauseAndContext() {
        JsonProcessingException root = new JsonProcessingException("inventory serialize fail") {
        };

        FulfillmentInventorySyncedEventSerializationException ex =
                new FulfillmentInventorySyncedEventSerializationException(
                        "fulfillment.inventory.synced", "fulfillment-inventory-sync", "evt-1", root
                );

        assertThat(ex.getMessage())
                .contains("ERR_FULFILLMENT_INVENTORY_SYNCED_EVENT_01")
                .contains("풀필먼트 재고 동기화 이벤트")
                .contains("topic=fulfillment.inventory.synced")
                .contains("partitionKey=fulfillment-inventory-sync")
                .contains("eventId=evt-1");
        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex).hasCause(root);
        assertThat(ex).hasRootCause(root);
    }
}
