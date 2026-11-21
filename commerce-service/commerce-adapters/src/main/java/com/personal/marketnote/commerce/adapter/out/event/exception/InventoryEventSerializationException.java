package com.personal.marketnote.commerce.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class InventoryEventSerializationException extends BaseEventSerializationException {

    public InventoryEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_INVENTORY_EVENT_01", "재고 이벤트", topic, partitionKey, eventId, cause);
    }
}
