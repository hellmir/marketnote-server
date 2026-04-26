package com.personal.marketnote.common.kafka.event;

public record SettlementExecutedEvent(
        Long settlementId,
        Long sellerId,
        Long totalAllocatedAmount,
        Long shippingFee,
        Long pgFeeAmount,
        Long platformFeeAmount,
        Long sellerPayoutAmount
) {
}
