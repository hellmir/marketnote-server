package com.personal.marketnote.product.port.in.result.shipping;

import com.personal.marketnote.product.domain.shipping.ShippingPolicy;

public record UpdateShippingPolicyResult(
        Long id,
        String deliveryCompany,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge
) {

    public static UpdateShippingPolicyResult from(ShippingPolicy shippingPolicy) {
        return new UpdateShippingPolicyResult(
                shippingPolicy.getId(),
                shippingPolicy.getDeliveryCompany(),
                shippingPolicy.getShippingFee(),
                shippingPolicy.getFreeShippingThreshold(),
                shippingPolicy.getJejuSurcharge(),
                shippingPolicy.getIslandSurcharge()
        );
    }
}
