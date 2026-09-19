package com.personal.marketnote.commerce.domain.ledger;

import com.personal.marketnote.common.domain.money.Money;
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
        boolean hasNonPositiveAmount = entries.stream()
                .anyMatch(entry -> !entry.getAmount().isPositive());
        if (hasNonPositiveAmount) {
            throw new InvalidLedgerEntryAmountException();
        }

        Money debitTotal = Money.zero();
        for (LedgerEntry entry : entries) {
            if (entry.getTransactionType().isDebit()) {
                debitTotal = debitTotal.add(entry.getAmount());
            }
        }

        Money creditTotal = Money.zero();
        for (LedgerEntry entry : entries) {
            if (entry.getTransactionType().isCredit()) {
                creditTotal = creditTotal.add(entry.getAmount());
            }
        }

        if (!debitTotal.equals(creditTotal)) {
            throw new LedgerEntryImbalanceException(debitTotal.getValue(), creditTotal.getValue());
        }
    }
}
