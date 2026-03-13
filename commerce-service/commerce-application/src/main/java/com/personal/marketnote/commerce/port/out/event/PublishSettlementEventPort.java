package com.personal.marketnote.commerce.port.out.event;

public interface PublishSettlementEventPort {

    void publishSettlementExecutedEvent(Long settlementId, Long sellerId,
                                        Long totalAllocatedAmount, Long pgFeeAmount,
                                        Long platformFeeAmount, Long sellerPayoutAmount);
}
