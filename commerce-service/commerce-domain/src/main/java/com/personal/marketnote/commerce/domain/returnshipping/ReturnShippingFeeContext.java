package com.personal.marketnote.commerce.domain.returnshipping;

import com.personal.marketnote.common.domain.money.Money;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReturnShippingFeeContext {
    private FaultType faultType;
    private InitialShippingType initialShippingType;
    private ReturnType returnType;
    private Money remainingAmount;
    private Money freeShippingThreshold;
    private Money shippingFee;

    public static ReturnShippingFeeContext of(
            FaultType faultType,
            InitialShippingType initialShippingType,
            ReturnType returnType,
            Money remainingAmount,
            Money freeShippingThreshold,
            Money shippingFee
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
        return remainingAmount.isLessThan(freeShippingThreshold);
    }
}
