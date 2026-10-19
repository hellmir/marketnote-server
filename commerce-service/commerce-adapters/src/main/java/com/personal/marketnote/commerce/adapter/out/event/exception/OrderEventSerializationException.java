package com.personal.marketnote.commerce.adapter.out.event.exception;

public class OrderEventSerializationException extends RuntimeException {
    public OrderEventSerializationException(String topic, Throwable cause) {
        super("ERR_ORDER_EVENT_01::주문 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
