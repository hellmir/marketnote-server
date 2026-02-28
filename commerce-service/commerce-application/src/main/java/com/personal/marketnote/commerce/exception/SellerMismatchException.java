package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidValueException;

/**
 * 주문 상품의 판매자 ID가 실제 상품의 판매자 ID와 불일치할 때 발생하는 예외
 *
 * @Author 성효빈
 * @Date 2026-02-27
 * @Description 주문 등록 시 sellerId가 product-service의 실제 판매자 ID와 일치하지 않으면 발생합니다.
 */
public class SellerMismatchException extends InvalidValueException {
    private static final String MESSAGE =
            "ERR_ORDER_SELLER_01::주문 상품의 판매자가 실제 상품의 판매자와 일치하지 않습니다.";

    public SellerMismatchException(Long pricePolicyId, Long requestedSellerId, Long actualSellerId) {
        super(MESSAGE);
    }
}
