package com.personal.marketnote.fulfillment.exception;

public class ShippingTrackerAlreadyExistsException extends RuntimeException {

    public ShippingTrackerAlreadyExistsException(Long orderId) {
        super("이미 배송 추적이 등록된 주문입니다. orderId=" + orderId);
    }
}
