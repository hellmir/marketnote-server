package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.in.result.post.PostProductInfoResult;
import com.personal.marketnote.community.port.out.result.product.ProductOptionInfoResult;

import java.util.List;

public record PostProductInfoResponse(
        String name,
        String brandName,
        List<ProductOptionInfoResult> selectedOptions
) {
    public static PostProductInfoResponse from(PostProductInfoResult result) {
        if (FormatValidator.hasValue(result)) {
            return new PostProductInfoResponse(
                    result.name(),
                    result.brandName(),
                    result.selectedOptions()
            );
        }

        return null;
    }
}
