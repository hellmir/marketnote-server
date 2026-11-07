package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SettlementPolicyCreateState {
    private Long sellerId;
    private FeeRate pgFeeRate;
    private FeeRate platformFeeRate;
    private SettlementCycle settlementCycle;
    private Long minPayoutAmount;

    public static SettlementPolicyCreateState forSeller(
            Long sellerId,
            FeeRate pgFeeRate,
            FeeRate platformFeeRate,
            SettlementCycle settlementCycle,
            Long minPayoutAmount
    ) {
        return SettlementPolicyCreateState.builder()
                .sellerId(sellerId)
                .pgFeeRate(pgFeeRate)
                .platformFeeRate(platformFeeRate)
                .settlementCycle(settlementCycle)
                .minPayoutAmount(minPayoutAmount)
                .build();
    }
}
