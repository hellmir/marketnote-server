package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class OrderNotFoundException extends DomainNotFoundException {
    private static final String ORDER_NOT_FOUND_EXCEPTION_MESSAGE = "주문을 찾을 수 없습니다. 전송된 주문 ID: %d";

    public OrderNotFoundException(Long orderId) {
        super(String.format(ORDER_NOT_FOUND_EXCEPTION_MESSAGE, orderId));
    }
}
