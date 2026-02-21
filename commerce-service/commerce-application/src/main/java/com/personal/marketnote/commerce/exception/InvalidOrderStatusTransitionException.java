package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatus;

public class InvalidOrderStatusTransitionException extends IllegalStateException {
    private static final String INVALID_ORDER_STATUS_TRANSITION_EXCEPTION_MESSAGE
            = "주문 상태를 %s에서 %s(으)로 변경할 수 없습니다.";

    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format(
                INVALID_ORDER_STATUS_TRANSITION_EXCEPTION_MESSAGE,
                from.getDescription(),
                to.getDescription()
        ));
    }
}
