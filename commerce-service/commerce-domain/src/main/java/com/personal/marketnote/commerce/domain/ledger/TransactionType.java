package com.personal.marketnote.commerce.domain.ledger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    DEBIT("차변"),
    CREDIT("대변");

    private final String description;

    public boolean isDebit() {
        return this == DEBIT;
    }

    public boolean isCredit() {
        return this == CREDIT;
    }
}
