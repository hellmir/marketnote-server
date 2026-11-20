package com.personal.marketnote.commerce.adapter.out.event.exception;

public class PaymentEventSerializationException extends RuntimeException {
    public PaymentEventSerializationException(String topic, Throwable cause) {
        super("ERR_PAYMENT_EVENT_01::결제 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
