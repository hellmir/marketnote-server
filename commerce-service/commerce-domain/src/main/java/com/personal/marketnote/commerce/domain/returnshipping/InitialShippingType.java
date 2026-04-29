package com.personal.marketnote.commerce.domain.returnshipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InitialShippingType {
    FREE_SHIPPING("무료 배송"),
    PAID_SHIPPING("유료 배송");

    private final String description;

    public static InitialShippingType from(long shippingFee) {
        if (shippingFee <= 0) {
            return FREE_SHIPPING;
        }
        return PAID_SHIPPING;
    }

    public boolean isFreeShipping() {
        return this == FREE_SHIPPING;
    }

    public boolean isPaidShipping() {
        return this == PAID_SHIPPING;
    }
}
