package com.personal.marketnote.commerce.domain.returnshipping;

import com.personal.marketnote.common.domain.money.Money;

public class ReturnShippingFeeCalculator {

    private ReturnShippingFeeCalculator() {
    }

    public static Money calculate(ReturnShippingFeeContext context) {
        if (context.isSellerFault()) {
            return Money.zero();
        }

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
}
