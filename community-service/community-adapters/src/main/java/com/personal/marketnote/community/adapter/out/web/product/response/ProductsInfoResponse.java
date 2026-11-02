package com.personal.marketnote.community.adapter.out.web.product.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.result.product.ProductOptionInfoResult;
import com.personal.marketnote.community.port.out.result.product.ProductPricePolicyInfoResult;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductsInfoResponse(
        Long sellerId,
        String name,
        String brandName,
        ProductPricePolicyInfoResult pricePolicy,
        List<ProductOptionInfoResult> selectedOptions,
        GetFileResult catalogImage
) {
    public Long getPricePolicyId() {
        return FormatValidator.hasValue(pricePolicy) ? pricePolicy.id() : null;
    }
}
