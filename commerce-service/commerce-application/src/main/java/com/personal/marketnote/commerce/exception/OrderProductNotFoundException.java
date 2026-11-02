package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class OrderProductNotFoundException extends DomainNotFoundException {
    private static final String ORDER_PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE = "주문 상품을 찾을 수 없습니다. 전송된 주문 ID: %d, 가격 정책 ID: %d";

    public OrderProductNotFoundException(Long id, Long pricePolicyId) {
        super(String.format(ORDER_PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE, id, pricePolicyId));
    }
}
