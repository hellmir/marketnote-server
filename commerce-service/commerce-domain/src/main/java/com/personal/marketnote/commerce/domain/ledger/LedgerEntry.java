package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LedgerEntry {
    private Long id;
    private Long accountId;
    private Long transactionId;
    private Long amount;
    private TransactionType transactionType;
    private LocalDateTime createdAt;

    public static LedgerEntry from(LedgerEntryCreateState state) {
        return LedgerEntry.builder()
                .accountId(state.getAccountId())
                .amount(state.getAmount())
                .transactionType(state.getTransactionType())
                .build();
    }

    public static LedgerEntry from(LedgerEntrySnapshotState state) {
        return LedgerEntry.builder()
                .id(state.getId())
                .accountId(state.getAccountId())
                .transactionId(state.getTransactionId())
                .amount(state.getAmount())
                .transactionType(state.getTransactionType())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public void assignTransaction(Long transactionId) {
        this.transactionId = transactionId;
    }
}
