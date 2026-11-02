package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import lombok.Getter;

@Getter
public class OrderStatusAlreadyChangedException extends IllegalStateException {
    private static final String ORDER_STATUS_ALREADY_CHANGED_EXCEPTION_MESSAGE
            = "이미 해당 주문 상태(%s)로 변경되었습니다.";

    public OrderStatusAlreadyChangedException(OrderStatus orderStatus) {
        super(String.format(ORDER_STATUS_ALREADY_CHANGED_EXCEPTION_MESSAGE, orderStatus.getDescription()));
    }
}
