package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.in.web.option.response.SelectableProductOptionCategoryResponse;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetAdminProductDetailResponse {
    private GetProductInfoResult productInfo;
    private List<SelectableProductOptionCategoryResponse> categories;
    private List<GetFileResult> representativeImages;
    private List<GetFileResult> contentImages;
    private List<GetProductPricePolicyWithOptionsResult> pricePolicies;
    private FasstoGoodsItemResponse fulfillmentGoods;
    private FasstoGoodsElementResponse fulfillmentGoodsElement;

    public static GetAdminProductDetailResponse from(GetAdminProductDetailResult result) {
        GetProductInfoResponse productResponse = GetProductInfoResponse.from(result.product());

        return GetAdminProductDetailResponse.builder()
                .productInfo(productResponse.getProductInfo())
                .categories(productResponse.getCategories())
                .representativeImages(productResponse.getRepresentativeImages())
                .contentImages(productResponse.getContentImages())
                .pricePolicies(productResponse.getPricePolicies())
                .fulfillmentGoods(
                        FormatValidator.hasValue(result.fulfillmentGoods())
                                ? FasstoGoodsItemResponse.from(result.fulfillmentGoods())
                                : null
                )
                .fulfillmentGoodsElement(
                        FormatValidator.hasValue(result.fulfillmentGoodsElement())
                                ? FasstoGoodsElementResponse.from(result.fulfillmentGoodsElement())
                                : null
                )
                .build();
    }
}
