package com.personal.marketnote.commerce.exception;

public class OrderCancellationNotAllowedException extends IllegalStateException {
    public OrderCancellationNotAllowedException(Long orderId) {
        super("ERR_ORDER_CANCEL_01::피킹완료 이후에는 주문 취소가 불가능합니다. 반품으로 처리해 주세요. orderId=" + orderId);
    }
}
