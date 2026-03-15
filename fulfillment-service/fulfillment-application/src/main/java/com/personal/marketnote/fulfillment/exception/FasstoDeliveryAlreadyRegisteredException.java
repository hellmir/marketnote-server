package com.personal.marketnote.fulfillment.exception;

public class FasstoDeliveryAlreadyRegisteredException extends RuntimeException {
    public FasstoDeliveryAlreadyRegisteredException(Long orderId) {
        super("이미 Fassto에 출고 요청된 주문입니다. orderId=" + orderId);
    }
}
