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
}
