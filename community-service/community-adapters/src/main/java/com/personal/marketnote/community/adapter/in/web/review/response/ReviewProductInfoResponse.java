package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.in.result.review.ReviewProductInfoResult;

public record ReviewProductInfoResponse(
        String name,
        String brandName,
        ReviewProductPricePolicyResponse pricePolicy,
        GetFileResult catalogImage
) {
    public static ReviewProductInfoResponse from(ReviewProductInfoResult result) {
        if (FormatValidator.hasNoValue(result)) {
            return null;
        }

        return new ReviewProductInfoResponse(
                result.name(),
                result.brandName(),
                ReviewProductPricePolicyResponse.from(result.pricePolicy()),
                result.catalogImage()
        );
    }
}
