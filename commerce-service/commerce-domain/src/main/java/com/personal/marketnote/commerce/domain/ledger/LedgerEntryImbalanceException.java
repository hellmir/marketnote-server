package com.personal.marketnote.commerce.domain.ledger;

public class LedgerEntryImbalanceException extends IllegalStateException {
    public LedgerEntryImbalanceException(long debitTotal, long creditTotal) {
        super("차변 합계(" + debitTotal + ")와 대변 합계(" + creditTotal + ")가 일치하지 않습니다.");
    }
}
