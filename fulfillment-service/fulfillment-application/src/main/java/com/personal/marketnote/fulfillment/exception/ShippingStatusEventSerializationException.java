package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ShippingStatusEventSerializationException extends BaseEventSerializationException {

    public ShippingStatusEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_SHIPPING_STATUS_EVENT_01", "배송 상태 변경 이벤트", topic, partitionKey, eventId, cause);
    }
}
