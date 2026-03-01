package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LedgerTransaction {
    private Long id;
    private LedgerTransactionType transactionType;
    private String targetType;
    private Long targetId;
    private String description;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public static LedgerTransaction from(LedgerTransactionCreateState state) {
        return LedgerTransaction.builder()
                .transactionType(state.getTransactionType())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .description(state.getDescription())
                .idempotencyKey(state.getIdempotencyKey())
                .build();
    }

    public static LedgerTransaction from(LedgerTransactionSnapshotState state) {
        return LedgerTransaction.builder()
                .id(state.getId())
                .transactionType(state.getTransactionType())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .description(state.getDescription())
                .idempotencyKey(state.getIdempotencyKey())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public void validateEntries(List<LedgerEntry> entries) {
        boolean hasNegativeOrZeroAmount = entries.stream()
                .anyMatch(entry -> entry.getAmount() <= 0);
        if (hasNegativeOrZeroAmount) {
            throw new InvalidLedgerEntryAmountException();
        }

        long debitTotal = 0L;
        for (LedgerEntry entry : entries) {
            if (entry.getTransactionType().isDebit()) {
                debitTotal = Math.addExact(debitTotal, entry.getAmount());
            }
        }

        long creditTotal = 0L;
        for (LedgerEntry entry : entries) {
            if (entry.getTransactionType().isCredit()) {
                creditTotal = Math.addExact(creditTotal, entry.getAmount());
            }
        }

        if (debitTotal != creditTotal) {
            throw new LedgerEntryImbalanceException(debitTotal, creditTotal);
        }
    }
}
