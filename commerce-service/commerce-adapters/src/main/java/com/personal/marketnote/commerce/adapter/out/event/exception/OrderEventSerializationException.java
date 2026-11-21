package com.personal.marketnote.commerce.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class OrderEventSerializationException extends BaseEventSerializationException {

    public OrderEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_ORDER_EVENT_01", "주문 이벤트", topic, partitionKey, eventId, cause);
    }
}
