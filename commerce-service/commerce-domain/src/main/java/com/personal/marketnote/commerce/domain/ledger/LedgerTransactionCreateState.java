package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LedgerTransactionCreateState {
    private LedgerTransactionType transactionType;
    private String targetType;
    private Long targetId;
    private String description;
    private IdempotencyKey idempotencyKey;

    public static LedgerTransactionCreateState forTransaction(
            LedgerTransactionType transactionType,
            String targetType,
            Long targetId,
            String description,
            IdempotencyKey idempotencyKey
    ) {
        return LedgerTransactionCreateState.builder()
                .transactionType(transactionType)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
