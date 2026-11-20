package com.personal.marketnote.commerce.adapter.out.event.exception;

public class SettlementEventSerializationException extends RuntimeException {
    public SettlementEventSerializationException(String topic, Throwable cause) {
        super("ERR_SETTLEMENT_EVENT_01::정산 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
