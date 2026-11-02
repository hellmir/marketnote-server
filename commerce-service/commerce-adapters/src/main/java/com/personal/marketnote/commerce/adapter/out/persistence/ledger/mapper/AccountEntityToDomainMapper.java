package com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.AccountJpaEntity;
import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.AccountSnapshotState;

public class AccountEntityToDomainMapper {

    private AccountEntityToDomainMapper() {
    }

    public static Account toDomain(AccountJpaEntity entity) {
        return Account.from(AccountSnapshotState.builder()
                .id(entity.getId())
                .name(entity.getName())
                .accountType(entity.getAccountType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build());
    }
}
