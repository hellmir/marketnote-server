package com.personal.marketnote.commerce.domain.ledger;

public class IdempotencyKeyNoValueException extends RuntimeException {
    public IdempotencyKeyNoValueException(String message) {
        super(message);
    }
}
