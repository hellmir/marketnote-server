package com.personal.marketnote.community.port.in.result.post;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.port.out.result.product.ProductOptionInfoResult;

import java.util.List;

public record PostProductInfoResult(
        String name,
        String brandName,
        List<ProductOptionInfoResult> selectedOptions
) {
    public static PostProductInfoResult from(ProductInfoResult productInfo) {
        if (FormatValidator.hasValue(productInfo)) {
            return new PostProductInfoResult(
                    productInfo.name(),
                    productInfo.brandName(),
                    productInfo.selectedOptions()
            );
        }

        return null;
    }
}
