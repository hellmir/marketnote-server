package com.personal.marketnote.user.adapter.out.event.exception;

public class ShippingAddressEventSerializationException extends RuntimeException {
    public ShippingAddressEventSerializationException(String topic, Throwable cause) {
        super("ERR_SHIPPING_ADDRESS_EVENT_01::배송지 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
