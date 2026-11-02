package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.result.option.SelectableProductOptionCategoryItemResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetProductInfoWithOptionsResult(
        GetProductInfoResult productInfo,
        List<SelectableProductOptionCategoryItemResult> categories,
        GetFilesResult representativeImages,
        GetFilesResult contentImages,
        List<GetProductPricePolicyWithOptionsResult> pricePolicies
) {
    public static GetProductInfoWithOptionsResult of(
            GetProductInfoResult productInfo,
            List<SelectableProductOptionCategoryItemResult> categories
    ) {
        return GetProductInfoWithOptionsResult.builder()
                .productInfo(productInfo)
                .categories(categories)
                .build();
    }

    public static GetProductInfoWithOptionsResult of(
            GetProductInfoResult productInfo,
            List<SelectableProductOptionCategoryItemResult> categories,
            GetFilesResult representativeImages,
            GetFilesResult contentImages,
            List<PricePolicy> pricePolicies
    ) {
        return GetProductInfoWithOptionsResult.builder()
                .productInfo(productInfo)
                .categories(categories)
                .representativeImages(representativeImages)
                .contentImages(contentImages)
                .pricePolicies(
                        pricePolicies.stream()
                                .map(GetProductPricePolicyWithOptionsResult::from)
                                .toList()
                )
                .build();
    }
}
