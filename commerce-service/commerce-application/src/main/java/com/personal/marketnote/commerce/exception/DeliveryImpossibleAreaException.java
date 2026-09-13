package com.personal.marketnote.commerce.exception;

public class DeliveryImpossibleAreaException extends RuntimeException {

    public DeliveryImpossibleAreaException() {
        super("ERR_ORDER_01::배송 불가 지역으로 주문할 수 없습니다.");
    }
}
