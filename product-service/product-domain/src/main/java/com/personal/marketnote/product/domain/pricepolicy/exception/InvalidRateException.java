package com.personal.marketnote.product.domain.pricepolicy.exception;

public class InvalidRateException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_PRICE_POLICY_RATE_01::비율 값이 유효하지 않습니다. %s";

    public InvalidRateException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
