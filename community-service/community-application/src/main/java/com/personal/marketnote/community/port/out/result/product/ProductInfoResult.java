package com.personal.marketnote.community.port.out.result.product;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

public record ProductInfoResult(
        Long sellerId,
        String name,
        String brandName,
        ProductPricePolicyInfoResult pricePolicy,
        List<ProductOptionInfoResult> selectedOptions,
        GetFileResult catalogImage
) {
    public boolean isMyProduct(Long userId) {
        return FormatValidator.equals(userId, sellerId);
    }
}
