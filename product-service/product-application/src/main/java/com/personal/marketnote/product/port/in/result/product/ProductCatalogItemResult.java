package com.personal.marketnote.product.port.in.result.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ProductCatalogItemResult {
    private final Long pricePolicyId;
    private final Long productId;
    private final Long sellerId;
    private final Long primaryCategoryId;
    private final String name;
    private final String brandName;
    private final Long price;
    private final Long discountPrice;
    private final BigDecimal discountRate;
    private final Long accumulatedPoint;
    private final BigDecimal accumulatedPointRate;
    private final Long orderNum;
    private final Long popularity;
    private final String representativeImageUrl;
    private final List<String> resizedImageUrls;
    private final Integer stockQuantity;
    private final Integer reviewCount;
    private final BigDecimal reviewAverageRating;
    private final Long sales;
    private final List<ProductCatalogTagSnapshot> productTags;
    private final List<ProductCatalogOptionSnapshot> selectedOptions;
    private final String status;

    @JsonCreator
    public ProductCatalogItemResult(
            @JsonProperty("pricePolicyId") Long pricePolicyId,
            @JsonProperty("productId") Long productId,
            @JsonProperty("sellerId") Long sellerId,
            @JsonProperty("primaryCategoryId") Long primaryCategoryId,
            @JsonProperty("name") String name,
            @JsonProperty("brandName") String brandName,
            @JsonProperty("price") Long price,
            @JsonProperty("discountPrice") Long discountPrice,
            @JsonProperty("discountRate") BigDecimal discountRate,
            @JsonProperty("accumulatedPoint") Long accumulatedPoint,
            @JsonProperty("accumulatedPointRate") BigDecimal accumulatedPointRate,
            @JsonProperty("orderNum") Long orderNum,
            @JsonProperty("popularity") Long popularity,
            @JsonProperty("representativeImageUrl") String representativeImageUrl,
            @JsonProperty("resizedImageUrls") List<String> resizedImageUrls,
            @JsonProperty("stockQuantity") Integer stockQuantity,
            @JsonProperty("reviewCount") Integer reviewCount,
            @JsonProperty("reviewAverageRating") BigDecimal reviewAverageRating,
            @JsonProperty("sales") Long sales,
            @JsonProperty("productTags") List<ProductCatalogTagSnapshot> productTags,
            @JsonProperty("selectedOptions") List<ProductCatalogOptionSnapshot> selectedOptions,
            @JsonProperty("status") String status
    ) {
        this.pricePolicyId = pricePolicyId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.primaryCategoryId = primaryCategoryId;
        this.name = name;
        this.brandName = brandName;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.accumulatedPoint = accumulatedPoint;
        this.accumulatedPointRate = accumulatedPointRate;
        this.orderNum = orderNum;
        this.popularity = popularity;
        this.representativeImageUrl = representativeImageUrl;
        this.resizedImageUrls = resizedImageUrls;
        this.stockQuantity = stockQuantity;
        this.reviewCount = reviewCount;
        this.reviewAverageRating = reviewAverageRating;
        this.sales = sales;
        this.productTags = productTags;
        this.selectedOptions = selectedOptions;
        this.status = status;
    }

    public Long pricePolicyId() {
        return pricePolicyId;
    }

    public Long productId() {
        return productId;
    }

    public Long sellerId() {
        return sellerId;
    }

    public Long primaryCategoryId() {
        return primaryCategoryId;
    }

    public String name() {
        return name;
    }

    public String brandName() {
        return brandName;
    }

    public Long price() {
        return price;
    }

    public Long discountPrice() {
        return discountPrice;
    }

    public BigDecimal discountRate() {
        return discountRate;
    }

    public Long accumulatedPoint() {
        return accumulatedPoint;
    }

    public BigDecimal accumulatedPointRate() {
        return accumulatedPointRate;
    }

    public Long orderNum() {
        return orderNum;
    }

    public Long popularity() {
        return popularity;
    }

    public String representativeImageUrl() {
        return representativeImageUrl;
    }

    public List<String> resizedImageUrls() {
        return resizedImageUrls;
    }

    public Integer stockQuantity() {
        return stockQuantity;
    }

    public Integer reviewCount() {
        return reviewCount;
    }

    public BigDecimal reviewAverageRating() {
        return reviewAverageRating;
    }

    public Long sales() {
        return sales;
    }

    public List<ProductCatalogTagSnapshot> productTags() {
        return productTags;
    }

    public List<ProductCatalogOptionSnapshot> selectedOptions() {
        return selectedOptions;
    }

    public String status() {
        return status;
    }
}
