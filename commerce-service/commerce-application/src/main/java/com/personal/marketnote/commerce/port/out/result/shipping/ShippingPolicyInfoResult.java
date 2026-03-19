package com.personal.marketnote.commerce.port.out.result.shipping;

public record ShippingPolicyInfoResult(
        Long sellerId,
        Long shippingFee,
        Long freeShippingThreshold
) {
}
