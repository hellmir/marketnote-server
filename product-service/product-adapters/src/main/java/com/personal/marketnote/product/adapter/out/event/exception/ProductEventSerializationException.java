package com.personal.marketnote.product.adapter.out.event.exception;

public class ProductEventSerializationException extends RuntimeException {
    public ProductEventSerializationException(String topic, Throwable cause) {
        super("ERR_PRODUCT_EVENT_01::상품 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
