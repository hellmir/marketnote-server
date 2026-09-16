package com.personal.marketnote.fulfillment.exception;

public class FulfillmentDeliveryWorkStatusChangedEventSerializationException extends RuntimeException {

    public FulfillmentDeliveryWorkStatusChangedEventSerializationException(Throwable cause) {
        super("풀필먼트 배송 작업 상태 변경 이벤트 직렬화 실패", cause);
    }
}
