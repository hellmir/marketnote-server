package com.personal.marketnote.product.adapter.in.web.shipping.response;

import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;

public record UpdateShippingPolicyResponse(
        Long id,
        String deliveryCompany,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge
) {

    public static UpdateShippingPolicyResponse from(UpdateShippingPolicyResult result) {
        return new UpdateShippingPolicyResponse(
                result.id(),
                result.deliveryCompany(),
                result.shippingFee(),
                result.freeShippingThreshold(),
                result.jejuSurcharge(),
                result.islandSurcharge()
        );
    }
}
