package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 포인트 잔액이 부족할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문 등록 시 요청 포인트가 보유 포인트를 초과하면 발생합니다.
 */
public class InsufficientPointException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_POINT_01::포인트 잔액이 부족합니다. 요청 포인트: %d, 보유 포인트: %d";

    public InsufficientPointException(Long requestedAmount, Long availableAmount) {
        super(String.format(MESSAGE, requestedAmount, availableAmount));
    }
}
