package com.personal.marketnote.commerce.port.in.result.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetLedgerTransactionResult(
        Long id,
        LedgerTransactionType transactionType,
        String targetType,
        Long targetId,
        String description,
        String idempotencyKey,
        LocalDateTime createdAt,
        List<GetLedgerEntryResult> entries
) {
    public static GetLedgerTransactionResult from(LedgerTransaction transaction, List<GetLedgerEntryResult> entries) {
        return GetLedgerTransactionResult.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .targetType(transaction.getTargetType())
                .targetId(transaction.getTargetId())
                .description(transaction.getDescription())
                .idempotencyKey(transaction.getIdempotencyKey())
                .createdAt(transaction.getCreatedAt())
                .entries(entries)
                .build();
    }
}
