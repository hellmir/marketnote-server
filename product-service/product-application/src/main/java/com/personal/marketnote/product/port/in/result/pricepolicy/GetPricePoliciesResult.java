package com.personal.marketnote.product.port.in.result.pricepolicy;

import java.util.List;

public record GetPricePoliciesResult(
        List<GetProductPricePolicyWithOptionsResult> policies
) {
    public static GetPricePoliciesResult of(List<GetProductPricePolicyWithOptionsResult> policies) {
        return new GetPricePoliciesResult(policies);
    }
}


