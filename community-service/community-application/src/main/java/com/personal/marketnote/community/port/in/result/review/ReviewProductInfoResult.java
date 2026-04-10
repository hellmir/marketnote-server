package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;

public record ReviewProductInfoResult(
        String name,
        String brandName,
        ReviewProductPricePolicyResult pricePolicy,
        GetFileResult catalogImage
) {
    public static ReviewProductInfoResult from(ProductInfoResult productInfo) {
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }

        return new ReviewProductInfoResult(
                productInfo.name(),
                productInfo.brandName(),
                ReviewProductPricePolicyResult.from(productInfo.pricePolicy()),
                productInfo.catalogImage()
        );
    }
}
