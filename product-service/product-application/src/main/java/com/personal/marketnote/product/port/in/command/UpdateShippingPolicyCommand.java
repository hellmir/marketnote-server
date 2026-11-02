package com.personal.marketnote.product.port.in.command;

public record UpdateShippingPolicyCommand(
        String deliveryCompany,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge
) {
}
