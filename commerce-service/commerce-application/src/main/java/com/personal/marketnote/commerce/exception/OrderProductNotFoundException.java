package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class OrderProductNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "주문 상품을 찾을 수 없습니다.";

    public OrderProductNotFoundException() {
        super(MESSAGE);
    }
}
