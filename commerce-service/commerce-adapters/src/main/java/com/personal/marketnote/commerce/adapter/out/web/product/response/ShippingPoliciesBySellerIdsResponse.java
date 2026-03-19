package com.personal.marketnote.commerce.adapter.out.web.product.response;

import java.util.List;

public record ShippingPoliciesBySellerIdsResponse(
        List<ShippingPolicyBySellerResponse> shippingPolicies
) {

    public record ShippingPolicyBySellerResponse(
            Long sellerId,
            Long shippingFee,
            Long freeShippingThreshold
    ) {
    }
}
