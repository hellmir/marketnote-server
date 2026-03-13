package com.personal.marketnote.common.kafka.event;

public record SettlementExecutedEvent(
        Long settlementId,
        Long sellerId,
        Long totalAllocatedAmount,
        Long pgFeeAmount,
        Long platformFeeAmount,
        Long sellerPayoutAmount
) {
}
