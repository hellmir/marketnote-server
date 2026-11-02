package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 클라이언트 전송 배송비와 서버 계산 배송비가 불일치할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 주문 등록 시 클라이언트가 전송한 shippingFee가 판매자별 배송비 정책 기반 계산 결과와 일치하지 않으면 발생합니다.
 */
public class ShippingFeeMismatchException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_SHIPPING_01::배송비가 서버 계산 결과와 일치하지 않습니다.";

    public ShippingFeeMismatchException(Long requestedShippingFee, Long calculatedShippingFee) {
        super(MESSAGE);
    }
}
