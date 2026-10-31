package com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.common.utility.FormatValidator;
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

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private Long accumulatedPoint;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal accumulatedPointRate;

    @Column(nullable = false)
    private Long orderNum;

    @Column(nullable = false)
    private Long popularity;

    @Column(length = 2048)
    private String representativeImageUrl;

    @Column(columnDefinition = "TEXT")
    private String resizedImageUrls;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer reviewCount;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal reviewAverageRating;

    @Column(nullable = false)
    private Long sales;

    @Column(columnDefinition = "TEXT")
    private String productTags;

    @Column(columnDefinition = "TEXT")
    private String selectedOptions;

    public static ProductCatalogReadModelJpaEntity of(
            Long pricePolicyId,
            Long productId,
            Long sellerId,
            Long primaryCategoryId,
            String name,
            String brandName,
            Long price,
            Long discountPrice,
            BigDecimal discountRate,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate,
            Long orderNum,
            Long popularity,
            Long sales,
            String productTags,
            String selectedOptions
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
                .discountRate(discountRate)
                .accumulatedPoint(accumulatedPoint)
                .accumulatedPointRate(accumulatedPointRate)
                .orderNum(orderNum)
                .popularity(popularity)
                .representativeImageUrl(null)
                .resizedImageUrls(null)
                .stockQuantity(0)
                .reviewCount(0)
                .reviewAverageRating(BigDecimal.ZERO)
                .sales(sales)
                .productTags(productTags)
                .selectedOptions(selectedOptions)
                .build();
    }

    public void updateProductInfo(String name, String brandName, Long primaryCategoryId) {
        if (FormatValidator.hasValue(name)) {
            this.name = name;
        }
        if (FormatValidator.hasValue(brandName)) {
            this.brandName = brandName;
        }
        if (FormatValidator.hasValue(primaryCategoryId)) {
            this.primaryCategoryId = primaryCategoryId;
        }
        activate();
    }

    public void updatePricing(
            Long price, Long discountPrice, BigDecimal discountRate,
            Long accumulatedPoint, BigDecimal accumulatedPointRate
    ) {
        if (FormatValidator.hasValue(price)) {
            this.price = price;
        }
        if (FormatValidator.hasValue(discountPrice)) {
            this.discountPrice = discountPrice;
        }
        if (FormatValidator.hasValue(discountRate)) {
            this.discountRate = discountRate;
        }
        if (FormatValidator.hasValue(accumulatedPoint)) {
            this.accumulatedPoint = accumulatedPoint;
        }
        if (FormatValidator.hasValue(accumulatedPointRate)) {
            this.accumulatedPointRate = accumulatedPointRate;
        }
    }

    public void updateOrderNum(Long orderNum) {
        if (FormatValidator.hasNoValue(orderNum)) {
            return;
        }
        this.orderNum = orderNum;
    }

    public void updatePopularity(Long popularity) {
        if (FormatValidator.hasNoValue(popularity)) {
            return;
        }
        this.popularity = popularity;
    }

    public void updateStockQuantity(Integer stockQuantity) {
        if (FormatValidator.hasNoValue(stockQuantity)) {
            return;
        }
        this.stockQuantity = stockQuantity;
    }

    public void updateRepresentativeImage(String representativeImageUrl, String resizedImageUrls) {
        if (FormatValidator.hasNoValue(representativeImageUrl)) {
            return;
        }
        this.representativeImageUrl = representativeImageUrl;
        this.resizedImageUrls = resizedImageUrls;
    }

    public void updateReviewAggregate(Integer reviewCount, BigDecimal reviewAverageRating) {
        if (FormatValidator.hasNoValue(reviewCount) || FormatValidator.hasNoValue(reviewAverageRating)) {
            return;
        }
        this.reviewCount = reviewCount;
        this.reviewAverageRating = reviewAverageRating;
    }

    public void updateSales(Long sales) {
        if (FormatValidator.hasNoValue(sales)) {
            return;
        }
        this.sales = sales;
    }

    public void increaseSales(Long delta) {
        long currentSales = FormatValidator.hasValue(sales) ? sales : 0L;
        long addDelta = FormatValidator.hasValue(delta) ? delta : 0L;
        this.sales = Math.addExact(currentSales, addDelta);
    }

    public void updateProductTags(String productTags) {
        this.productTags = productTags;
    }

    public void updateSelectedOptions(String selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public void markInactive() {
        deactivate();
    }
}
