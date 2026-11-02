package com.personal.marketnote.commerce.adapter.out.event.exception;

public class ReturnTrackerEventSerializationException extends IllegalStateException {

    public ReturnTrackerEventSerializationException(String partitionKey, Throwable cause) {
        super("반품 추적 이벤트 직렬화 실패. orderId=" + partitionKey, cause);
    }
}
