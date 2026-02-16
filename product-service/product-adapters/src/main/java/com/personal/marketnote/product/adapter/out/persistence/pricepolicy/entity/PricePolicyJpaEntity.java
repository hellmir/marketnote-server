package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Table(name = "price_policy")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PricePolicyJpaEntity extends BaseOrderedGeneralEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_price_policy_product"))
    private ProductJpaEntity productJpaEntity;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "discount_price", nullable = false)
    private Long discountPrice;

    @Column(name = "discount_rate", nullable = false, precision = 3, scale = 1)
    private BigDecimal discountRate;

    @Column(name = "accumulated_point", nullable = false)
    private Long accumulatedPoint;

    @Column(name = "accumulation_rate", nullable = false, precision = 3, scale = 1)
    private BigDecimal accumulationRate;

    @Column(name = "popularity", nullable = false, insertable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long popularity;

    @OneToMany(mappedBy = "pricePolicyJpaEntity", cascade = {PERSIST, MERGE})
    private List<ProductOptionPricePolicyJpaEntity> productOptionPricePolicyJpaEntities;

    public static PricePolicyJpaEntity from(ProductJpaEntity productRef, PricePolicy pricePolicy) {
        return PricePolicyJpaEntity.builder()
                .productJpaEntity(productRef)
                .price(pricePolicy.getPrice())
                .discountPrice(pricePolicy.getDiscountPrice())
                .discountRate(pricePolicy.getDiscountRate())
                .accumulatedPoint(pricePolicy.getAccumulatedPoint())
                .accumulationRate(pricePolicy.getAccumulationRate())
                .build();
    }

    public void deactivate() {
        super.deactivate();
    }
}
