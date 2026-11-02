package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatus;

public class PartialCancelNotAllowedException extends IllegalStateException {
    public PartialCancelNotAllowedException(OrderStatus orderStatus) {
        super("ERR_ORDER_PARTIAL_CANCEL_01::현재 주문 상태에서 부분 취소가 불가능합니다. orderStatus=" + orderStatus.name());
    }
}
