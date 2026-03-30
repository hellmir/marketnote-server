package com.personal.marketnote.fulfillment.exception;

public class FulfillmentDeliveryAlreadyRegisteredException extends RuntimeException {
    public FulfillmentDeliveryAlreadyRegisteredException(Long orderId) {
        super("이미 Fulfillment에 출고 요청된 주문입니다. orderId=" + orderId);
    }
}
