package com.personal.marketnote.product.port.in.result.shipping;

import com.personal.marketnote.product.domain.shipping.ShippingPolicy;

public record GetShippingPolicyResult(
        Long id,
        String deliveryCompany,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge
) {

    public static GetShippingPolicyResult from(ShippingPolicy shippingPolicy) {
        return new GetShippingPolicyResult(
                shippingPolicy.getId(),
                shippingPolicy.getDeliveryCompany(),
                shippingPolicy.getShippingFee(),
                shippingPolicy.getFreeShippingThreshold(),
                shippingPolicy.getJejuSurcharge(),
                shippingPolicy.getIslandSurcharge()
        );
    }
}
