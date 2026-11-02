package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LedgerEntrySnapshotState {
    private Long id;
    private Long accountId;
    private Long transactionId;
    private Long amount;
    private TransactionType transactionType;
    private LocalDateTime createdAt;
}
