package com.personal.marketnote.commerce.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class ReturnTrackerEventSerializationException extends BaseEventSerializationException {

    public ReturnTrackerEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_RETURN_TRACKER_EVENT_01", "반품 추적 이벤트", topic, partitionKey, eventId, cause);
    }
}
