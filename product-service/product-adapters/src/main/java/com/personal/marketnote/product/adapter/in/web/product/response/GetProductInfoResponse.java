package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.in.web.option.response.SelectableProductOptionCategoryResponse;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetProductInfoResponse {
    private GetProductInfoResult productInfo;
    private List<SelectableProductOptionCategoryResponse> categories;
    private List<GetFileResult> representativeImages;
    private List<GetFileResult> contentImages;
    private List<GetProductPricePolicyWithOptionsResult> pricePolicies;

    public static GetProductInfoResponse from(GetProductInfoWithOptionsResult getProductInfoWithOptionsResult) {
        GetFilesResult representativeImages = getProductInfoWithOptionsResult.representativeImages();
        GetFilesResult contentImages = getProductInfoWithOptionsResult.contentImages();

        return GetProductInfoResponse.builder()
                .productInfo(getProductInfoWithOptionsResult.productInfo())
                .categories(
                        getProductInfoWithOptionsResult.categories().stream()
                                .map(SelectableProductOptionCategoryResponse::from)
                                .toList()
                )
                .representativeImages(
                        FormatValidator.hasValue(representativeImages)
                                ? representativeImages.images()
                                : null
                )
                .contentImages(
                        FormatValidator.hasValue(contentImages)
                                ? contentImages.images()
                                : null
                )
                .pricePolicies(getProductInfoWithOptionsResult.pricePolicies())
                .build();
    }
}


