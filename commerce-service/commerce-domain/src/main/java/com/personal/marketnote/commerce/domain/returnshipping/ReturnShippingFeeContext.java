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
    private Money regionSurcharge;

    public static ReturnShippingFeeContext from(ReturnShippingFeeContextCreateState state) {
        return ReturnShippingFeeContext.builder()
                .faultType(state.getFaultType())
                .initialShippingType(state.getInitialShippingType())
                .returnType(state.getReturnType())
                .remainingAmount(state.getRemainingAmount())
                .freeShippingThreshold(state.getFreeShippingThreshold())
                .shippingFee(state.getShippingFee())
                .regionSurcharge(state.getRegionSurcharge())
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
