package com.personal.marketnote.commerce.domain.payment;

public class InvalidInstallmentException extends IllegalArgumentException {
    public InvalidInstallmentException(String message) {
        super(message);
    }
}
