package com.personal.marketnote.commerce.domain.payment;

public class InvalidRefundAmountException extends IllegalArgumentException {
    public InvalidRefundAmountException(String message) {
        super(message);
    }
}
