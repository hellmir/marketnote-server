package com.personal.marketnote.commerce.adapter.out.web.product.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.commerce.port.out.product.result.ProductOptionInfoResult;
import com.personal.marketnote.commerce.port.out.product.result.ProductPricePolicyInfoResult;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductsInfoResponse(
        Long id,
        Long sellerId,
        String name,
        String brandName,
        ProductPricePolicyInfoResult pricePolicy,
        List<ProductOptionInfoResult> selectedOptions
) {
    public Long getPricePolicyId() {
        return pricePolicy.id();
    }
}
