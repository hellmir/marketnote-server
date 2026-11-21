package com.personal.marketnote.commerce.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class SettlementEventSerializationException extends BaseEventSerializationException {

    public SettlementEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_SETTLEMENT_EVENT_01", "정산 이벤트", topic, partitionKey, eventId, cause);
    }
}
