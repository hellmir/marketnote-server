package com.personal.marketnote.commerce.port.in.result.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetSettlementPolicyResult(
        Long id,
        Long sellerId,
        Integer pgFeeRate,
        Integer platformFeeRate,
        SettlementCycle settlementCycle,
        Long minPayoutAmount,
        EntityStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static GetSettlementPolicyResult from(SettlementPolicy policy) {
        return GetSettlementPolicyResult.builder()
                .id(policy.getId())
                .sellerId(policy.getSellerId())
                .pgFeeRate(policy.getPgFeeRate())
                .platformFeeRate(policy.getPlatformFeeRate())
                .settlementCycle(policy.getSettlementCycle())
                .minPayoutAmount(policy.getMinPayoutAmount())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .modifiedAt(policy.getModifiedAt())
                .build();
    }
}
