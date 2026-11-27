package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class FulfillmentInventorySyncedEventSerializationException extends BaseEventSerializationException {

    public FulfillmentInventorySyncedEventSerializationException(
        String topic, String partitionKey, String eventId, Throwable cause
    ) {
        super(
            "ERR_FULFILLMENT_INVENTORY_SYNCED_EVENT_01",
            "풀필먼트 재고 동기화 이벤트",
            topic, partitionKey, eventId, cause
        );
    }
}
