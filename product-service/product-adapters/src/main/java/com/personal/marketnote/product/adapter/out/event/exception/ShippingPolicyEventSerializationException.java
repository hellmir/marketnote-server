package com.personal.marketnote.product.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ShippingPolicyEventSerializationException extends BaseEventSerializationException {

    public ShippingPolicyEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_SHIPPING_POLICY_EVENT_01", "배송 정책 이벤트", topic, partitionKey, eventId, cause);
    }
}
