package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatus;

public class InvalidOrderStatusForPaymentException extends IllegalStateException {
    private static final String INVALID_ORDER_STATUS_FOR_PAYMENT_EXCEPTION_MESSAGE
            = "결제 대기 상태에서만 거래를 등록할 수 있습니다. 현재 주문 상태: %s";

    public InvalidOrderStatusForPaymentException(OrderStatus currentStatus) {
        super(String.format(INVALID_ORDER_STATUS_FOR_PAYMENT_EXCEPTION_MESSAGE, currentStatus.getDescription()));
    }
}
