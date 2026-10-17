package com.personal.marketnote.product.adapter.out.persistence.productcatalog.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ProductCatalogReadModelJpaEntityToResultMapper {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private ProductCatalogReadModelJpaEntityToResultMapper() {
    }

    public static ProductCatalogItemResult mapToResult(
            ProductCatalogReadModelJpaEntity entity, ObjectMapper objectMapper
    ) {
        return new ProductCatalogItemResult(
                entity.getPricePolicyId(),
                entity.getProductId(),
                entity.getSellerId(),
                entity.getPrimaryCategoryId(),
                entity.getName(),
                entity.getBrandName(),
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getDiscountRate(),
                entity.getAccumulatedPoint(),
                entity.getAccumulatedPointRate(),
                entity.getOrderNum(),
                entity.getPopularity(),
                entity.getRepresentativeImageUrl(),
                parseResizedImageUrls(entity.getPricePolicyId(), entity.getResizedImageUrls(), objectMapper),
                entity.getStockQuantity(),
                entity.getReviewCount(),
                entity.getReviewAverageRating(),
                entity.getStatus().name()
        );
    }

    private static List<String> parseResizedImageUrls(Long pricePolicyId, String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (IOException ioe) {
            log.warn("상품 카탈로그 Read Model resized 이미지 JSON 역직렬화 실패. pricePolicyId={}, json={}",
                    pricePolicyId, json, ioe);
            return List.of();
        }
    }
}
