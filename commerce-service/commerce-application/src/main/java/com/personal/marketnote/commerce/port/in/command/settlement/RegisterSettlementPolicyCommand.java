package com.personal.marketnote.commerce.port.in.command.settlement;

import lombok.Builder;

@Builder
public record RegisterSettlementPolicyCommand(
        Long sellerId,
        Integer pgFeeRate,
        Integer platformFeeRate,
        String settlementCycle,
        Long minPayoutAmount
) {
}
