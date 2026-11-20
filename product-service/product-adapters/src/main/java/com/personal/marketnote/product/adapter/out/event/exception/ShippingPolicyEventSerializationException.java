package com.personal.marketnote.product.adapter.out.event.exception;

public class ShippingPolicyEventSerializationException extends RuntimeException {
    public ShippingPolicyEventSerializationException(String topic, Throwable cause) {
        super("ERR_SHIPPING_POLICY_EVENT_01::배송 정책 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
