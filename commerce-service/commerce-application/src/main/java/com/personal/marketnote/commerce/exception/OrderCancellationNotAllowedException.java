package com.personal.marketnote.commerce.exception;

public class OrderCancellationNotAllowedException extends RuntimeException {
    public OrderCancellationNotAllowedException(Long orderId, String message) {
        super("ERR_ORDER_CANCEL_01::주문 취소가 불가합니다. orderId=" + orderId + ", reason=" + message);
    }
}
