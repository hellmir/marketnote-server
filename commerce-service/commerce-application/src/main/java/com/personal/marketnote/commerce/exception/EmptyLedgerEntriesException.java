package com.personal.marketnote.commerce.exception;

public class EmptyLedgerEntriesException extends IllegalArgumentException {
    private static final String MESSAGE = "분개 항목이 비어있습니다.";

    public EmptyLedgerEntriesException() {
        super(MESSAGE);
    }
}
