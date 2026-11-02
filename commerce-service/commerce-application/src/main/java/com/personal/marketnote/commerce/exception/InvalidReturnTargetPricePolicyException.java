package com.personal.marketnote.commerce.exception;

import java.util.List;

public class InvalidReturnTargetPricePolicyException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_ORDER_PARTIAL_RETURN_01::반품 요청 품목이 주문에 존재하지 않습니다. invalidPricePolicyIds=%s";

    public InvalidReturnTargetPricePolicyException(List<Long> invalidPricePolicyIds) {
        super(String.format(MESSAGE, invalidPricePolicyIds));
    }
}
