package com.personal.marketnote.fulfillment.exception;

public class FulfillmentDeliveryRegistrationNotFoundException extends RuntimeException {
    public FulfillmentDeliveryRegistrationNotFoundException(Long orderId) {
        super("orderId에 해당하는 출고 등록을 찾을 수 없습니다. orderId=" + orderId);
    }
}
