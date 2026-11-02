package com.personal.marketnote.product.adapter.in.web.shipping.response;

import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;

public record RegisterShippingPolicyResponse(Long id) {

    public static RegisterShippingPolicyResponse from(RegisterShippingPolicyResult result) {
        return new RegisterShippingPolicyResponse(result.id());
    }
}
