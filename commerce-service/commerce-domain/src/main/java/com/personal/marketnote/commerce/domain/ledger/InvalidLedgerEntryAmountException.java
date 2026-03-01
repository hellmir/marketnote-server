package com.personal.marketnote.commerce.domain.ledger;

public class InvalidLedgerEntryAmountException extends IllegalStateException {
    private static final String MESSAGE = "분개 금액은 0보다 커야 합니다.";

    public InvalidLedgerEntryAmountException() {
        super(MESSAGE);
    }
}
