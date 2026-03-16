package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductItemResult {
    private Long id;
    private Long sellerId;
    private String name;
    private String brandName;
    private GetProductPricePolicyResult pricePolicy;
    private Integer sales;
    private List<ProductTag> productTags;
    private GetFileResult catalogImage;
    private List<ProductOptionItemResult> selectedOptions;
    private Integer stock;
    private Float averageRating;
    private Integer totalCount;
    private String status;
    private Long orderNum;

    public static ProductItemResult from(Product product) {
        return ProductItemResult.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .brandName(product.getBrandName())
                .pricePolicy(GetProductPricePolicyResult.from(product.getDefaultPricePolicy()))
                .sales(product.getSales())
                .productTags(getTopOrderedTags(product.getProductTags()))
                .status(product.getStatus().name())
                .orderNum(product.getOrderNum())
                .build();
    }

    public static ProductItemResult from(
            Product product,
            PricePolicy pricePolicy
    ) {
        return ProductItemResult.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .brandName(product.getBrandName())
                .pricePolicy(GetProductPricePolicyResult.from(pricePolicy))
                .sales(product.getSales())
                .productTags(getTopOrderedTags(product.getProductTags()))
                .status(product.getStatus().name())
                .orderNum(product.getOrderNum())
                .build();
    }

    public static ProductItemResult from(
            Product product,
            List<ProductOption> selectedOptions,
            PricePolicy pricePolicy
    ) {
        return ProductItemResult.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .brandName(product.getBrandName())
                .pricePolicy(GetProductPricePolicyResult.from(pricePolicy))
                .sales(product.getSales())
                .productTags(getTopOrderedTags(product.getProductTags()))
                .selectedOptions(selectedOptions.stream()
                        .map(ProductOptionItemResult::from)
                        .toList())
                .status(product.getStatus().name())
                .orderNum(product.getOrderNum())
                .build();
    }

    public static ProductItemResult from(
            ProductItemResult productItemResult, GetFileResult catalogImage, Integer stock
    ) {
        return ProductItemResult.builder()
                .id(productItemResult.getId())
                .sellerId(productItemResult.getSellerId())
                .name(productItemResult.getName())
                .brandName(productItemResult.getBrandName())
                .pricePolicy(productItemResult.getPricePolicy())
                .sales(productItemResult.getSales())
                .productTags(productItemResult.getProductTags())
                .catalogImage(catalogImage)
                .selectedOptions(productItemResult.getSelectedOptions())
                .stock(stock)
                .averageRating(productItemResult.getAverageRating())
                .totalCount(productItemResult.getTotalCount())
                .status(productItemResult.getStatus())
                .orderNum(productItemResult.getOrderNum())
                .build();
    }

    public static ProductItemResult from(
            ProductItemResult productItemResult,
            GetFileResult catalogImage,
            Integer stock,
            Float averageRating,
            Integer totalCount
    ) {
        return ProductItemResult.builder()
                .id(productItemResult.getId())
                .sellerId(productItemResult.getSellerId())
                .name(productItemResult.getName())
                .brandName(productItemResult.getBrandName())
                .pricePolicy(productItemResult.getPricePolicy())
                .sales(productItemResult.getSales())
                .productTags(productItemResult.getProductTags())
                .catalogImage(catalogImage)
                .selectedOptions(productItemResult.getSelectedOptions())
                .stock(stock)
                .averageRating(averageRating)
                .totalCount(totalCount)
                .status(productItemResult.getStatus())
                .orderNum(productItemResult.getOrderNum())
                .build();
    }

    public Long getPricePolicyId() {
        return pricePolicy.id();
    }

    private static final int MAX_DISPLAY_TAGS = 2;

    private static List<ProductTag> getTopOrderedTags(List<ProductTag> productTags) {
        return productTags.stream()
                .sorted(Comparator.comparingLong(ProductTag::getOrderNum))
                .limit(MAX_DISPLAY_TAGS)
                .toList();
    }
}
