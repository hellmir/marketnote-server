package com.personal.marketnote.fulfillment.exception;

public class ShippingStatusEventSerializationException extends RuntimeException {

    public ShippingStatusEventSerializationException(Long orderId, Throwable cause) {
        super("배송 상태 변경 이벤트 직렬화 실패. orderId=" + orderId, cause);
    }
}
