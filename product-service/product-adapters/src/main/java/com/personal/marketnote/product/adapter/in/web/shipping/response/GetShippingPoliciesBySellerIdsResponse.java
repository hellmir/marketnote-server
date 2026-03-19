package com.personal.marketnote.product.adapter.in.web.shipping.response;

import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;

import java.util.List;

public record GetShippingPoliciesBySellerIdsResponse(
        List<ShippingPolicyBySellerResponse> shippingPolicies
) {

    public static GetShippingPoliciesBySellerIdsResponse from(List<GetShippingPolicyBySellerResult> results) {
        List<ShippingPolicyBySellerResponse> responses = results.stream()
                .map(ShippingPolicyBySellerResponse::from)
                .toList();
        return new GetShippingPoliciesBySellerIdsResponse(responses);
    }

    public record ShippingPolicyBySellerResponse(
            Long sellerId,
            Long shippingFee,
            Long freeShippingThreshold
    ) {

        public static ShippingPolicyBySellerResponse from(GetShippingPolicyBySellerResult result) {
            return new ShippingPolicyBySellerResponse(
                    result.sellerId(),
                    result.shippingFee(),
                    result.freeShippingThreshold()
            );
        }
    }
}
