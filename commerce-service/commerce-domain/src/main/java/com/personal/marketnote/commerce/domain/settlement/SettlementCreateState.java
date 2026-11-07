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
    private Long shippingFee;
    private Long pgFeeAmount;
    private Long platformFeeAmount;
    private Long sellerPayoutAmount;

    public static SettlementCreateState forMonthly(
            Long sellerId,
            Integer year,
            Integer month,
            Long totalAllocatedAmount,
            Long shippingFee,
            Long pgFeeAmount,
            Long platformFeeAmount,
            Long sellerPayoutAmount
    ) {
        return SettlementCreateState.builder()
                .sellerId(sellerId)
                .year(year)
                .month(month)
                .totalAllocatedAmount(totalAllocatedAmount)
                .shippingFee(shippingFee)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .build();
    }
}
