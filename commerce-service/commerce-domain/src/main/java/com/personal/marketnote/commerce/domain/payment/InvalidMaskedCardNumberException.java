package com.personal.marketnote.commerce.domain.payment;

public class InvalidMaskedCardNumberException extends IllegalArgumentException {
    public InvalidMaskedCardNumberException(String message) {
        super(message);
    }
}
