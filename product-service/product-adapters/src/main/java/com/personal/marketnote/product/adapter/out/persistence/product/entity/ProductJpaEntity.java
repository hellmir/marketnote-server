package com.personal.marketnote.product.adapter.out.persistence.product.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductTag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.utility.EntityConstant.BOOLEAN_DEFAULT_FALSE;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Table(name = "product")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductJpaEntity extends BaseOrderedGeneralEntity {
    @Column(name = "product_key", nullable = false, unique = true, updatable = false)
    private UUID productKey;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "detail", length = 1023)
    private String detail;

    @Column(name = "stock", nullable = false, insertable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stock;

    @Column(name = "sales", nullable = false, insertable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sales;

    @Column(name = "view_count", nullable = false, insertable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount;

    @Column(name = "popularity", nullable = false, insertable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long popularity;

    @Column(name = "find_all_options_yn", nullable = false, columnDefinition = BOOLEAN_DEFAULT_FALSE)
    private boolean findAllOptionsYn;

    @OneToMany(mappedBy = "productJpaEntity", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private List<ProductTagJpaEntity> productTagJpaEntities = new ArrayList<>();

    @OneToMany(mappedBy = "productJpaEntity", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private List<PricePolicyJpaEntity> pricePolicyJpaEntities = new ArrayList<>();

    public static ProductJpaEntity from(Product product) {
        ProductJpaEntity productJpaEntity = ProductJpaEntity.builder()
                .productKey(product.getProductKey())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .brandName(product.getBrandName())
                .detail(product.getDetail())
                .findAllOptionsYn(product.isFindAllOptionsYn())
                .build();

        productJpaEntity.productTagJpaEntities = (
                product.getProductTags()
                        .stream()
                        .map(tag -> ProductTagJpaEntity.from(productJpaEntity, tag))
                        .collect(Collectors.toList())
        );

        return productJpaEntity;
    }

    public void updateFrom(Product product) {
        updateActivation(product);
        name = product.getName();
        brandName = product.getBrandName();
        detail = product.getDetail();
        findAllOptionsYn = product.isFindAllOptionsYn();

        productTagJpaEntities.clear();
        if (FormatValidator.hasValue(product.getProductTags())) {
            for (ProductTag tag : product.getProductTags()) {
                productTagJpaEntities.add(ProductTagJpaEntity.from(this, tag));
            }
        }
    }

    private void updateActivation(Product product) {
        if (product.isActive()) {
            activate();
            return;
        }

        if (product.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }

    public PricePolicyJpaEntity getDefaultPricePolicy() {
        if (FormatValidator.hasValue(pricePolicyJpaEntities)) {
            return pricePolicyJpaEntities.stream()
                    .max(Comparator.comparing(PricePolicyJpaEntity::getId))
                    .orElse(null);
        }

        return null;
    }

    public Long getPrice() {
        PricePolicyJpaEntity latest = getDefaultPricePolicy();
        return FormatValidator.hasValue(latest) ? latest.getPrice() : null;
    }

    public Long getDiscountPrice() {
        PricePolicyJpaEntity latest = getDefaultPricePolicy();
        return FormatValidator.hasValue(latest) ? latest.getDiscountPrice() : null;
    }

    public BigDecimal getDiscountRate() {
        PricePolicyJpaEntity latest = getDefaultPricePolicy();
        return FormatValidator.hasValue(latest) ? latest.getDiscountRate() : null;
    }

    public Long getAccumulatedPoint() {
        PricePolicyJpaEntity latest = getDefaultPricePolicy();
        return FormatValidator.hasValue(latest) ? latest.getAccumulatedPoint() : null;
    }
}
