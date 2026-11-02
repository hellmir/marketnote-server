package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatus;

public class InvalidOrderStatusForReviewException extends IllegalStateException {
    private static final String INVALID_ORDER_STATUS_FOR_REVIEW_EXCEPTION_MESSAGE
            = "리뷰 작성이 불가능한 주문 상태입니다. 현재 주문 상태: %s";

    public InvalidOrderStatusForReviewException(OrderStatus currentStatus) {
        super(String.format(INVALID_ORDER_STATUS_FOR_REVIEW_EXCEPTION_MESSAGE, currentStatus.getDescription()));
    }
}
