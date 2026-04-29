package com.personal.marketnote.commerce.domain.returnshipping;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReturnShippingFeeContext {
    private FaultType faultType;
    private InitialShippingType initialShippingType;
    private ReturnType returnType;
    private long remainingAmount;
    private long freeShippingThreshold;
    private long shippingFee;

    public static ReturnShippingFeeContext of(
            FaultType faultType,
            InitialShippingType initialShippingType,
            ReturnType returnType,
            long remainingAmount,
            long freeShippingThreshold,
            long shippingFee
    ) {
        return ReturnShippingFeeContext.builder()
                .faultType(faultType)
                .initialShippingType(initialShippingType)
                .returnType(returnType)
                .remainingAmount(remainingAmount)
                .freeShippingThreshold(freeShippingThreshold)
                .shippingFee(shippingFee)
                .build();
    }

    public boolean isSellerFault() {
        return faultType.isSeller();
    }

    public boolean wasPaidShipping() {
        return initialShippingType.isPaidShipping();
    }

    public boolean isFullReturn() {
        return returnType.isFullReturn();
    }

    public boolean isBelowFreeShippingThreshold() {
        return remainingAmount < freeShippingThreshold;
    }
}
