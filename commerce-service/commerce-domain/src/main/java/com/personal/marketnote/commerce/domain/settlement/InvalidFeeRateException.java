package com.personal.marketnote.commerce.domain.settlement;

public class InvalidFeeRateException extends IllegalArgumentException {
    public InvalidFeeRateException(String message) {
        super(message);
    }
}
