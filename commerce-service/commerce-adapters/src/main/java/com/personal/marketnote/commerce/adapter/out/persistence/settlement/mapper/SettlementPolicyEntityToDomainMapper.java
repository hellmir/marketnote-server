package com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementPolicyJpaEntity;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicySnapshotState;

public class SettlementPolicyEntityToDomainMapper {
    private SettlementPolicyEntityToDomainMapper() {
    }

    public static SettlementPolicy toDomain(SettlementPolicyJpaEntity entity) {
        return SettlementPolicy.from(
                SettlementPolicySnapshotState.builder()
                        .id(entity.getId())
                        .sellerId(entity.getSellerId())
                        .pgFeeRate(entity.getPgFeeRate())
                        .platformFeeRate(entity.getPlatformFeeRate())
                        .settlementCycle(entity.getSettlementCycle())
                        .minPayoutAmount(entity.getMinPayoutAmount())
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
