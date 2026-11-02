package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;

public class InvalidReasonCategoryException extends IllegalArgumentException {
    private static final String INVALID_REASON_CATEGORY_EXCEPTION_MESSAGE
            = "주문 상태 변경에 적합하지 않은 사유 카테고리입니다. reasonCategory=%s";

    public InvalidReasonCategoryException(OrderStatusReasonCategory reasonCategory) {
        super(String.format(INVALID_REASON_CATEGORY_EXCEPTION_MESSAGE, reasonCategory.name()));
    }
}
