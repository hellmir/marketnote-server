package com.personal.marketnote.commerce.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class PaymentEventSerializationException extends BaseEventSerializationException {

    public PaymentEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_PAYMENT_EVENT_01", "결제 이벤트", topic, partitionKey, eventId, cause);
    }
}
