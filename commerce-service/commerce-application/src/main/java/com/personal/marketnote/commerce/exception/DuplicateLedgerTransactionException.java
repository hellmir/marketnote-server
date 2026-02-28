package com.personal.marketnote.commerce.exception;

public class DuplicateLedgerTransactionException extends RuntimeException {
    public DuplicateLedgerTransactionException(String idempotencyKey) {
        super("이미 처리된 장부 거래입니다. 멱등성 키: " + idempotencyKey);
    }
}
