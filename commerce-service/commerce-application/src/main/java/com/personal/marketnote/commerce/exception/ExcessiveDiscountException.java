package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 할인 금액(쿠폰 + 포인트)이 총 주문 금액을 초과할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 주문 등록 시 couponAmount + pointAmount가 totalAmount를 초과하면 발생합니다.
 */
public class ExcessiveDiscountException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_DISCOUNT_01::할인 금액이 주문 총액을 초과합니다.";

    public ExcessiveDiscountException(Long totalAmount, Long couponAmount, Long pointAmount) {
        super(MESSAGE);
    }
}
