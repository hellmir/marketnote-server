package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 주문 상품의 단가가 실제 상품 가격과 불일치할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 주문 등록 시 unitAmount가 product-service의 실제 판매가와 일치하지 않으면 발생합니다.
 */
public class PriceMismatchException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_PRICE_01::주문 상품 단가가 실제 가격과 일치하지 않습니다.";

    public PriceMismatchException(Long pricePolicyId, Long requestedPrice, Long actualPrice) {
        super(MESSAGE);
    }
}
