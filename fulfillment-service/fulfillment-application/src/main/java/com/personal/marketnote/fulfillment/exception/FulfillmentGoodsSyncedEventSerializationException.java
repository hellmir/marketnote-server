package com.personal.marketnote.fulfillment.exception;

public class FulfillmentGoodsSyncedEventSerializationException extends RuntimeException {

    public FulfillmentGoodsSyncedEventSerializationException(Throwable cause) {
        super("풀필먼트 상품 동기화 이벤트 직렬화 실패", cause);
    }
}
