package com.personal.marketnote.commerce.adapter.in.web.settlement.response;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetSettlementPolicyResponse(
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
    public static GetSettlementPolicyResponse from(GetSettlementPolicyResult result) {
        return GetSettlementPolicyResponse.builder()
                .id(result.id())
                .sellerId(result.sellerId())
                .pgFeeRate(result.pgFeeRate())
                .platformFeeRate(result.platformFeeRate())
                .settlementCycle(result.settlementCycle())
                .minPayoutAmount(result.minPayoutAmount())
                .status(result.status())
                .createdAt(result.createdAt())
                .modifiedAt(result.modifiedAt())
                .build();
    }
}
