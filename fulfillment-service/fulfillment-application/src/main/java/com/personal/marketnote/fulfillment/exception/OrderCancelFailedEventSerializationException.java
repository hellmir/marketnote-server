package com.personal.marketnote.fulfillment.exception;

public class OrderCancelFailedEventSerializationException extends RuntimeException {

    public OrderCancelFailedEventSerializationException(Long orderId, Throwable cause) {
        super("주문 취소 실패 이벤트 직렬화 실패. orderId=" + orderId, cause);
    }
}
