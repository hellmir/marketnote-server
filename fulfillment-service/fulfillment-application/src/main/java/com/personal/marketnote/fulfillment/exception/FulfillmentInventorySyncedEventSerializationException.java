package com.personal.marketnote.fulfillment.exception;

public class FulfillmentInventorySyncedEventSerializationException extends RuntimeException {

    public FulfillmentInventorySyncedEventSerializationException(Throwable cause) {
        super("풀필먼트 재고 동기화 이벤트 직렬화 실패", cause);
    }
}
