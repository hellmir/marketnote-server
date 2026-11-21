package com.personal.marketnote.product.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ProductEventSerializationException extends BaseEventSerializationException {

    public ProductEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_PRODUCT_EVENT_01", "상품 이벤트", topic, partitionKey, eventId, cause);
    }
}
