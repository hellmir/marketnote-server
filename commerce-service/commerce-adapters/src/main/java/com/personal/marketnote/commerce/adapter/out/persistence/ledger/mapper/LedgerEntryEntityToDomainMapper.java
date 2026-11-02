package com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerEntryJpaEntity;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntrySnapshotState;

public class LedgerEntryEntityToDomainMapper {

    private LedgerEntryEntityToDomainMapper() {
    }

    public static LedgerEntry toDomain(LedgerEntryJpaEntity entity) {
        return LedgerEntry.from(LedgerEntrySnapshotState.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .transactionId(entity.getTransactionId())
                .amount(entity.getAmount())
                .transactionType(entity.getTransactionType())
                .createdAt(entity.getCreatedAt())
                .build());
    }
}
