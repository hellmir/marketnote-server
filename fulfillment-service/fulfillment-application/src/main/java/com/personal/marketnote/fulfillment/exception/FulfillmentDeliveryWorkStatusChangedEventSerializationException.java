package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class FulfillmentDeliveryWorkStatusChangedEventSerializationException extends BaseEventSerializationException {

    public FulfillmentDeliveryWorkStatusChangedEventSerializationException(
        String topic, String partitionKey, String eventId, Throwable cause
    ) {
        super(
            "ERR_FULFILLMENT_DELIVERY_WORK_STATUS_EVENT_01",
            "풀필먼트 배송 작업 상태 변경 이벤트",
            topic, partitionKey, eventId, cause
        );
    }
}
