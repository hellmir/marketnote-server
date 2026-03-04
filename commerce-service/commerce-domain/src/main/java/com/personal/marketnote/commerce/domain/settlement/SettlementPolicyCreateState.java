package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class SettlementPolicyCreateState {
    private Long sellerId;
    private Integer pgFeeRate;
    private Integer platformFeeRate;
    private SettlementCycle settlementCycle;
    private Long minPayoutAmount;
}
