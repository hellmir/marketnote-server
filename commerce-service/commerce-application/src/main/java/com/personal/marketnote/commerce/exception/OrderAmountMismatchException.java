package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 주문 총액과 상품별 금액 합계가 불일치할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 주문 등록 시 totalAmount가 Σ(unitAmount × quantity)와 일치하지 않으면 발생합니다.
 */
public class OrderAmountMismatchException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_AMOUNT_01::주문 총액이 상품 금액 합계와 일치하지 않습니다.";

    public OrderAmountMismatchException(Long requestedTotal, Long calculatedTotal) {
        super(MESSAGE);
    }
}
