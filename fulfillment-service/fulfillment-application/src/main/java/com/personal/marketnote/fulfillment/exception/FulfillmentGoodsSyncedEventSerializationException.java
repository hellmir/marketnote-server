package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class FulfillmentGoodsSyncedEventSerializationException extends BaseEventSerializationException {

    public FulfillmentGoodsSyncedEventSerializationException(
            String topic, String partitionKey, String eventId, Throwable cause
    ) {
        super(
                "ERR_FULFILLMENT_GOODS_SYNCED_EVENT_01",
                "풀필먼트 상품 동기화 이벤트",
                topic, partitionKey, eventId, cause
        );
    }
}
