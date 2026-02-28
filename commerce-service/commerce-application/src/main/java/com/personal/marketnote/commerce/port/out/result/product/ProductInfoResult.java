package com.personal.marketnote.commerce.port.out.result.product;

import com.personal.marketnote.commerce.port.out.product.result.ProductOptionInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

public record ProductInfoResult(
        Long id,
        Long sellerId,
        String name,
        String brandName,
        Long price,
        Long discountPrice,
        List<ProductOptionInfoResult> selectedOptions
) {
    /**
     * 실제 판매가를 반환한다.
     * discountPrice가 존재하면 할인가, 없으면 정가를 반환한다.
     */
    public Long getSellingPrice() {
        if (FormatValidator.hasValue(discountPrice)) {
            return discountPrice;
        }
        return price;
    }
}
