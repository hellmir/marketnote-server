package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LedgerEntryCreateState {
    private Long accountId;
    private Long amount;
    private TransactionType transactionType;

    public static LedgerEntryCreateState of(Long accountId, Long amount, TransactionType transactionType) {
        return LedgerEntryCreateState.builder()
                .accountId(accountId)
                .amount(amount)
                .transactionType(transactionType)
                .build();
    }
}
