package com.personal.marketnote.product.adapter.out.persistence.productoption.entity;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option_price_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionPricePolicyJpaEntity {
    @EmbeddedId
    private ProductOptionPricePolicyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productOptionId")
    @JoinColumn(name = "product_option_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pop_option"))
    private ProductOptionJpaEntity productOptionJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pricePolicyId")
    @JoinColumn(name = "price_policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pop_price_policy"))
    private PricePolicyJpaEntity pricePolicyJpaEntity;

    public static ProductOptionPricePolicyJpaEntity of(ProductOptionJpaEntity optionRef, PricePolicyJpaEntity pricePolicyRef) {
        return ProductOptionPricePolicyJpaEntity.builder()
                .id(new ProductOptionPricePolicyId(optionRef.getId(), pricePolicyRef.getId()))
                .productOptionJpaEntity(optionRef)
                .pricePolicyJpaEntity(pricePolicyRef)
                .build();
    }
}


