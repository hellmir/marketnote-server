package com.personal.marketnote.commerce.domain.shipping;

import com.personal.marketnote.common.domain.money.Money;

public class ShippingFeeCalculator {

    private ShippingFeeCalculator() {
    }

    public static Money calculateBaseFee(ShippingFeeContext context) {
        if (context.isBelowFreeShippingThreshold()) {
            return context.getShippingFee();
        }
        return Money.zero();
    }
}
