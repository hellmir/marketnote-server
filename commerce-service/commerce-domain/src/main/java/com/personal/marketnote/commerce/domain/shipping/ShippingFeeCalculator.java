package com.personal.marketnote.commerce.domain.shipping;

public class ShippingFeeCalculator {

    private ShippingFeeCalculator() {
    }

    public static long calculateBaseFee(ShippingFeeContext context) {
        if (context.isBelowFreeShippingThreshold()) {
            return context.getShippingFee();
        }
        return 0L;
    }
}
