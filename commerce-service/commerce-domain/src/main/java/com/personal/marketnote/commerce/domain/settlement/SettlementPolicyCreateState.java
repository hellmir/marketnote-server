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
}
