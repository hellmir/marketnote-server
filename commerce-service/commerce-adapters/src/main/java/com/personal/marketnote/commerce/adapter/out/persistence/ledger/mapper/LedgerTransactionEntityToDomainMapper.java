package com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerTransactionJpaEntity;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionSnapshotState;

public class LedgerTransactionEntityToDomainMapper {

    private LedgerTransactionEntityToDomainMapper() {
    }

    public static LedgerTransaction toDomain(LedgerTransactionJpaEntity entity) {
        return LedgerTransaction.from(LedgerTransactionSnapshotState.builder()
                .id(entity.getId())
                .transactionType(entity.getTransactionType())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .description(entity.getDescription())
                .idempotencyKey(entity.getIdempotencyKey())
                .createdAt(entity.getCreatedAt())
                .build());
    }
}
