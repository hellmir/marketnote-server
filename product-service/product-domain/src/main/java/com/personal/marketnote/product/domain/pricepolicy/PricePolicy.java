package com.personal.marketnote.product.domain.pricepolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.product.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PricePolicy extends BaseDomain {
    private Long id;
    private Long productId;

    @JsonIgnore
    private Product product;

    private Long price;
    private Long discountPrice;
    private BigDecimal discountRate;
    private Long accumulatedPoint;
    private BigDecimal accumulationRate;
    private Long popularity;
    private Long orderNum;
    private List<Long> optionIds;
    private List<ProductOption> productOptions;

    @JsonCreator
    private static PricePolicy jsonCreator(
            @JsonProperty("id") Long id,
            @JsonProperty("productId") Long productId,
            @JsonProperty("price") Long price,
            @JsonProperty("discountPrice") Long discountPrice,
            @JsonProperty("discountRate") BigDecimal discountRate,
            @JsonProperty("accumulatedPoint") Long accumulatedPoint,
            @JsonProperty("accumulationRate") BigDecimal accumulationRate,
            @JsonProperty("popularity") Long popularity,
            @JsonProperty("status") EntityStatus status,
            @JsonProperty("orderNum") Long orderNum,
            @JsonProperty("optionIds") List<Long> optionIds
    ) {
        return from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .productId(productId)
                        .price(price)
                        .discountPrice(discountPrice)
                        .discountRate(discountRate)
                        .accumulatedPoint(accumulatedPoint)
                        .accumulationRate(accumulationRate)
                        .popularity(popularity)
                        .status(status)
                        .orderNum(orderNum)
                        .optionIds(optionIds)
                        .build()
        );
    }

    public static PricePolicy from(PricePolicyCreateState state) {
        Product product = state.getProduct();
        Long productId = FormatValidator.hasValue(product) ? product.getId() : null;

        return PricePolicy.builder()
                .productId(productId)
                .product(product)
                .price(state.getPrice())
                .discountPrice(state.getDiscountPrice())
                .discountRate(state.getDiscountRate())
                .accumulatedPoint(state.getAccumulatedPoint())
                .accumulationRate(state.getAccumulationRate())
                .popularity(state.getPopularity())
                .orderNum(state.getOrderNum())
                .optionIds(state.getOptionIds())
                .build();
    }

    public static PricePolicy from(PricePolicySnapshotState state) {
        Product product = state.getProduct();
        Long productId = state.getProductId();
        if (FormatValidator.hasNoValue(productId) && FormatValidator.hasValue(product)) {
            productId = product.getId();
        }

        PricePolicy pricePolicy = PricePolicy.builder()
                .id(state.getId())
                .productId(productId)
                .product(product)
                .price(state.getPrice())
                .discountPrice(state.getDiscountPrice())
                .discountRate(state.getDiscountRate())
                .accumulatedPoint(state.getAccumulatedPoint())
                .accumulationRate(state.getAccumulationRate())
                .popularity(state.getPopularity())
                .orderNum(state.getOrderNum())
                .optionIds(state.getOptionIds())
                .productOptions(state.getProductOptions())
                .build();
        pricePolicy.status = state.getStatus();

        return pricePolicy;
    }

    public boolean hasOptions() {
        return FormatValidator.hasValue(optionIds);
    }

    public void addProduct(Product product) {
        this.product = product;
        if (FormatValidator.hasValue(product) && FormatValidator.hasNoValue(productId)) {
            this.productId = product.getId();
        }
    }

    public boolean hasProduct() {
        return FormatValidator.hasValue(productId);
    }

    public void setOptionIds() {
        optionIds = productOptions.stream()
                .map(ProductOption::getId)
                .toList();
    }
}
