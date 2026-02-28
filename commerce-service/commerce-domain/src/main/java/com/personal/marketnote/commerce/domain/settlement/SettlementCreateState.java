package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SettlementCreateState {
    private Long sellerId;
    private Integer year;
    private Integer month;
    private Long totalAllocatedAmount;
    private Long pgFeeAmount;
    private Long platformFeeAmount;
    private Long sellerPayoutAmount;
}
