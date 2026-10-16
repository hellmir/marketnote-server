package com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "product_catalog_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_catalog_read_model_price_policy_id",
                columnNames = "price_policy_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductCatalogReadModelJpaEntity extends BaseGeneralEntity {

    @Column(name = "price_policy_id", nullable = false)
    private Long pricePolicyId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long sellerId;

    @Column
    private Long primaryCategoryId;

    @Column(nullable = false)
    private String name;

    @Column
    private String brandName;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long discountPrice;

    @Column(nullable = false)
    private Long accumulatedPoint;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal accumulatedPointRate;

    @Column(nullable = false)
    private Long orderNum;

    @Column(nullable = false)
    private Long popularity;

    @Column(length = 511)
    private String representativeImageUrl;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer reviewCount;

    @Column(nullable = false)
    private Float reviewAverageRating;

    public static ProductCatalogReadModelJpaEntity of(
            Long pricePolicyId,
            Long productId,
            Long sellerId,
            Long primaryCategoryId,
            String name,
            String brandName,
            Long price,
            Long discountPrice,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate,
            Long orderNum,
            Long popularity
    ) {
        return ProductCatalogReadModelJpaEntity.builder()
                .pricePolicyId(pricePolicyId)
                .productId(productId)
                .sellerId(sellerId)
                .primaryCategoryId(primaryCategoryId)
                .name(name)
                .brandName(brandName)
                .price(price)
                .discountPrice(discountPrice)
                .accumulatedPoint(accumulatedPoint)
                .accumulatedPointRate(accumulatedPointRate)
                .orderNum(orderNum)
                .popularity(popularity)
                .representativeImageUrl(null)
                .stockQuantity(0)
                .reviewCount(0)
                .reviewAverageRating(0f)
                .build();
    }

    public void updateProductInfo(String name, String brandName, Long primaryCategoryId) {
        this.name = name;
        this.brandName = brandName;
        this.primaryCategoryId = primaryCategoryId;
        activate();
    }

    public void updatePricing(Long price, Long discountPrice, Long accumulatedPoint, BigDecimal accumulatedPointRate) {
        this.price = price;
        this.discountPrice = discountPrice;
        this.accumulatedPoint = accumulatedPoint;
        this.accumulatedPointRate = accumulatedPointRate;
    }

    public void updateOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public void updatePopularity(Long popularity) {
        this.popularity = popularity;
    }

    public void updateStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void updateRepresentativeImageUrl(String representativeImageUrl) {
        this.representativeImageUrl = representativeImageUrl;
    }

    public void updateReviewAggregate(Integer reviewCount, Float reviewAverageRating) {
        this.reviewCount = reviewCount;
        this.reviewAverageRating = reviewAverageRating;
    }

    public void markInactive() {
        deactivate();
    }
}
