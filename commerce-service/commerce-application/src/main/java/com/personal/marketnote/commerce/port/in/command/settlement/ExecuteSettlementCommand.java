package com.personal.marketnote.commerce.port.in.command.settlement;

import lombok.Builder;

@Builder
public record ExecuteSettlementCommand(
        Integer year,
        Integer month,
        Integer pgFeeRate,
        Integer platformFeeRate
) {
}
