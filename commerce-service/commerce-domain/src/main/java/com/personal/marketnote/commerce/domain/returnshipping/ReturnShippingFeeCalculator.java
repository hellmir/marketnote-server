package com.personal.marketnote.commerce.domain.returnshipping;

import com.personal.marketnote.common.domain.money.Money;

public class ReturnShippingFeeCalculator {

    private ReturnShippingFeeCalculator() {
    }

    public static Money calculate(ReturnShippingFeeContext context) {
        if (context.isSellerFault()) {
            return Money.zero();
        }

        Money baseFee = resolveBaseFee(context);
        Money surcharge = resolveSurcharge(context);
        return baseFee.add(surcharge);
    }

    private static Money resolveBaseFee(ReturnShippingFeeContext context) {
        if (context.wasPaidShipping()) {
            return context.getShippingFee();
        }
        if (context.isFullReturn()) {
            return context.getShippingFee().multiply(2L);
        }
        if (context.isBelowFreeShippingThreshold()) {
            return context.getShippingFee().multiply(2L);
        }
        return context.getShippingFee();
    }

    private static Money resolveSurcharge(ReturnShippingFeeContext context) {
        if (context.wasPaidShipping()) {
            return context.getRegionSurcharge();
        }
        if (context.isFullReturn()) {
            return context.getRegionSurcharge().multiply(2L);
        }
        if (context.isBelowFreeShippingThreshold()) {
            return context.getRegionSurcharge().multiply(2L);
        }
        return context.getRegionSurcharge();
    }
}
