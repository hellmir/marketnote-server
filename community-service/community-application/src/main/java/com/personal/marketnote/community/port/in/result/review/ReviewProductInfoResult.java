package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;

import java.util.Map;

public record ReviewProductInfoResult(
        String name,
        String brandName,
        ReviewProductPricePolicyResult pricePolicy,
        GetFileResult catalogImage,
        Long unitAmount
) {
    public static ReviewProductInfoResult from(ProductInfoResult productInfo) {
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }

        return new ReviewProductInfoResult(
                productInfo.name(),
                productInfo.brandName(),
                ReviewProductPricePolicyResult.from(productInfo.pricePolicy()),
                productInfo.catalogImage(),
                null
        );
    }

    public static ReviewProductInfoResult from(ProductInfoResult productInfo, Long unitAmount) {
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }

        return new ReviewProductInfoResult(
                productInfo.name(),
                productInfo.brandName(),
                ReviewProductPricePolicyResult.from(productInfo.pricePolicy()),
                productInfo.catalogImage(),
                unitAmount
        );
    }

    public ReviewProductInfoResult withUnitAmount(Long unitAmount) {
        return new ReviewProductInfoResult(name, brandName, pricePolicy, catalogImage, unitAmount);
    }

    public static ReviewProductInfoResult resolveForReview(
            Review review, Map<Long, ReviewProductInfoResult> productInfoByPricePolicyId
    ) {
        Long pricePolicyId = review.getPricePolicyId();
        if (FormatValidator.hasNoValue(pricePolicyId)) {
            return null;
        }
        ReviewProductInfoResult productInfo = productInfoByPricePolicyId.get(pricePolicyId);
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }
        if (!review.hasUnitAmount()) {
            return productInfo;
        }
        return productInfo.withUnitAmount(review.getUnitAmount());
    }
}
