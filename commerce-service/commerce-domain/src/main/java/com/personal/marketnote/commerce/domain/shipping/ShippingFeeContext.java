package com.personal.marketnote.commerce.domain.shipping;

import com.personal.marketnote.common.domain.money.Money;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingFeeContext {
    private Money sellerAmount;
    private Money shippingFee;
    private Money freeShippingThreshold;

    public static ShippingFeeContext of(Money sellerAmount, Money shippingFee, Money freeShippingThreshold) {
        return ShippingFeeContext.builder()
                .sellerAmount(sellerAmount)
                .shippingFee(shippingFee)
                .freeShippingThreshold(freeShippingThreshold)
                .build();
    }

    public boolean isBelowFreeShippingThreshold() {
        return sellerAmount.isLessThan(freeShippingThreshold);
    }
}
