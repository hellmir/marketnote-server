package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class OrderCancelFailedEventSerializationException extends BaseEventSerializationException {

    public OrderCancelFailedEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_ORDER_CANCEL_FAILED_EVENT_01", "주문 취소 실패 이벤트", topic, partitionKey, eventId, cause);
    }
}
