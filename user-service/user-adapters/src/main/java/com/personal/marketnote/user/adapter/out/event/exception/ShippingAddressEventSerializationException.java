package com.personal.marketnote.user.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ShippingAddressEventSerializationException extends BaseEventSerializationException {

    public ShippingAddressEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_SHIPPING_ADDRESS_EVENT_01", "배송지 이벤트", topic, partitionKey, eventId, cause);
    }
}
