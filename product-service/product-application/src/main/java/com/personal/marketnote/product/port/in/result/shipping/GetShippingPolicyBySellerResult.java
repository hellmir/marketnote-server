package com.personal.marketnote.product.port.in.result.shipping;

import com.personal.marketnote.product.domain.shipping.ShippingPolicy;

public record GetShippingPolicyBySellerResult(
        Long sellerId,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge
) {

    public static GetShippingPolicyBySellerResult from(ShippingPolicy shippingPolicy) {
        return new GetShippingPolicyBySellerResult(
                shippingPolicy.getSellerId(),
                shippingPolicy.getShippingFee(),
                shippingPolicy.getFreeShippingThreshold(),
                shippingPolicy.getJejuSurcharge(),
                shippingPolicy.getIslandSurcharge()
        );
    }
}
