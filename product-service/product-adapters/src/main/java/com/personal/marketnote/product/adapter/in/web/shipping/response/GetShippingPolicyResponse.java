package com.personal.marketnote.product.adapter.in.web.shipping.response;

import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;

public record GetShippingPolicyResponse(
        Long id,
        String deliveryCompany,
        Long shippingFee,
        Long freeShippingThreshold
) {

    public static GetShippingPolicyResponse from(GetShippingPolicyResult result) {
        return new GetShippingPolicyResponse(
                result.id(),
                result.deliveryCompany(),
                result.shippingFee(),
                result.freeShippingThreshold()
        );
    }
}
