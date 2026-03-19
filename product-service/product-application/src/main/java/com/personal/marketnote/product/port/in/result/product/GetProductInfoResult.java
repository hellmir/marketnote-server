package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyResult;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import lombok.Builder;

import java.util.List;

@Builder(access = lombok.AccessLevel.PRIVATE)
public record GetProductInfoResult(
        Long id,
        Long sellerId,
        String name,
        String brandName,
        String detail,
        GetProductPricePolicyResult selectedPricePolicy,
        Integer sales,
        Long viewCount,
        Long popularity,
        boolean findAllOptionsYn,
        List<ProductTag> productTags,
        Integer stock,
        Long orderNum,
        String status,
        GetShippingPolicyResult shippingPolicy
) {
    public static GetProductInfoResult from(
            Product product, PricePolicy pricePolicy, Integer stock, ShippingPolicy shippingPolicy
    ) {
        return GetProductInfoResult.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .brandName(product.getBrandName())
                .detail(product.getDetail())
                .selectedPricePolicy(GetProductPricePolicyResult.from(pricePolicy))
                .sales(product.getSales())
                .viewCount(product.getViewCount())
                .popularity(product.getPopularity())
                .findAllOptionsYn(product.isFindAllOptionsYn())
                .productTags(product.getProductTags())
                .stock(stock)
                .orderNum(product.getOrderNum())
                .status(product.getStatus().name())
                .shippingPolicy(
                        FormatValidator.hasValue(shippingPolicy) ? GetShippingPolicyResult.from(shippingPolicy) : null
                )
                .build();
    }
}
