package com.personal.marketnote.commerce.port.in.command.settlement;

import lombok.Builder;

@Builder
public record UpdateSettlementPolicyCommand(
        Long id,
        Integer pgFeeRate,
        Integer platformFeeRate,
        String settlementCycle,
        Long minPayoutAmount
) {
}
