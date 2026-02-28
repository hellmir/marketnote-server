package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LedgerTransactionSnapshotState {
    private Long id;
    private LedgerTransactionType transactionType;
    private String targetType;
    private Long targetId;
    private String description;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
