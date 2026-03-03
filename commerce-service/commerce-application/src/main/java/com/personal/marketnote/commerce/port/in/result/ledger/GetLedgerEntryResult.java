package com.personal.marketnote.commerce.port.in.result.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetLedgerEntryResult(
        Long id,
        Long accountId,
        Long transactionId,
        Long amount,
        TransactionType transactionType,
        LocalDateTime createdAt
) {
    public static GetLedgerEntryResult from(LedgerEntry entry) {
        return GetLedgerEntryResult.builder()
                .id(entry.getId())
                .accountId(entry.getAccountId())
                .transactionId(entry.getTransactionId())
                .amount(entry.getAmount())
                .transactionType(entry.getTransactionType())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
