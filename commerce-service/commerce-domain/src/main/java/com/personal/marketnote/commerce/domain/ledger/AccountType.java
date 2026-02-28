package com.personal.marketnote.commerce.domain.ledger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    ASSET("자산"),
    LIABILITY("부채"),
    EQUITY("자본"),
    REVENUE("수익"),
    EXPENSE("비용");

    private final String description;

    public boolean isAsset() {
        return this == ASSET;
    }

    public boolean isLiability() {
        return this == LIABILITY;
    }

    public boolean isRevenue() {
        return this == REVENUE;
    }

    public boolean isExpense() {
        return this == EXPENSE;
    }
}
