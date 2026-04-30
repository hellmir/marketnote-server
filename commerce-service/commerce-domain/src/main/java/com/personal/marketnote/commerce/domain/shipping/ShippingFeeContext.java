package com.personal.marketnote.commerce.domain.shipping;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingFeeContext {
    private long sellerAmount;
    private long shippingFee;
    private long freeShippingThreshold;

    public static ShippingFeeContext of(long sellerAmount, long shippingFee, long freeShippingThreshold) {
        return ShippingFeeContext.builder()
                .sellerAmount(sellerAmount)
                .shippingFee(shippingFee)
                .freeShippingThreshold(freeShippingThreshold)
                .build();
    }

    public boolean isBelowFreeShippingThreshold() {
        return sellerAmount < freeShippingThreshold;
    }
}
