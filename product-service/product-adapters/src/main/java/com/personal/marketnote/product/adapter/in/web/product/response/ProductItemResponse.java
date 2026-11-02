package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyResult;
import com.personal.marketnote.product.port.in.result.product.ProductItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductTagItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductItemResponse {
    private Long id;
    private Long sellerId;
    private String name;
    private String brandName;
    private GetProductPricePolicyResult pricePolicy;
    private Integer sales;
    private List<ProductTagItem> productTags;
    private GetFileResult catalogImage;
    private List<ProductOptionItemResult> selectedOptions;
    private Integer stock;
    private BigDecimal averageRating;
    private Integer totalCount;
    private String status;
    private Long orderNum;

    public static ProductItemResponse from(ProductItemResult result) {
        return ProductItemResponse.builder()
                .id(result.getId())
                .sellerId(result.getSellerId())
                .name(result.getName())
                .brandName(result.getBrandName())
                .pricePolicy(result.getPricePolicy())
                .sales(result.getSales())
                .productTags(result.getProductTags())
                .catalogImage(result.getCatalogImage())
                .selectedOptions(result.getSelectedOptions())
                .stock(result.getStock())
                .averageRating(result.getAverageRating())
                .totalCount(result.getTotalCount())
                .status(result.getStatus())
                .orderNum(result.getOrderNum())
                .build();
    }
}

