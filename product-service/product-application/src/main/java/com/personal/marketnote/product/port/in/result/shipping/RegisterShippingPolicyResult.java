package com.personal.marketnote.product.port.in.result.shipping;

public record RegisterShippingPolicyResult(Long id) {

    public static RegisterShippingPolicyResult of(Long id) {
        return new RegisterShippingPolicyResult(id);
    }
}
