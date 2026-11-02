package com.personal.marketnote.fulfillment.domain.exception;

public class ShippingTrackerNotFoundException extends RuntimeException {

    public ShippingTrackerNotFoundException(Long orderId) {
        super("배송 추적 정보를 찾을 수 없습니다. orderId=" + orderId);
    }
}
