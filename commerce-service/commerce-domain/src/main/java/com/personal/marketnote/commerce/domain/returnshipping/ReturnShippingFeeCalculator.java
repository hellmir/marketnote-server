package com.personal.marketnote.commerce.domain.returnshipping;

public class ReturnShippingFeeCalculator {

    private ReturnShippingFeeCalculator() {
    }

    public static long calculate(ReturnShippingFeeContext context) {
        if (context.isSellerFault()) {
            return 0L;
        }

        if (context.wasPaidShipping()) {
            return context.getShippingFee();
        }

        if (context.isFullReturn()) {
            return Math.multiplyExact(context.getShippingFee(), 2L);
        }

        if (context.isBelowFreeShippingThreshold()) {
            return Math.multiplyExact(context.getShippingFee(), 2L);
        }

        return context.getShippingFee();
    }
}
