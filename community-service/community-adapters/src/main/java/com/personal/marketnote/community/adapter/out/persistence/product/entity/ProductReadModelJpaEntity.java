package com.personal.marketnote.community.adapter.out.persistence.product.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_read_models",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_read_model_price_policy_id", columnNames = "pricePolicyId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private Long pricePolicyId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String brandName;

    private Long price;

    private Long discountPrice;

    private Long accumulatedPoint;

    public static ProductReadModelJpaEntity of(
            Long pricePolicyId, Long productId, Long sellerId, String name,
            String brandName, Long price, Long discountPrice, Long accumulatedPoint
    ) {
        return ProductReadModelJpaEntity.builder()
                .pricePolicyId(pricePolicyId)
                .productId(productId)
                .sellerId(sellerId)
                .name(name)
                .brandName(brandName)
                .price(price)
                .discountPrice(discountPrice)
                .accumulatedPoint(accumulatedPoint)
                .build();
    }

    public void updateFrom(Long productId, Long sellerId, String name,
                           String brandName, Long price, Long discountPrice, Long accumulatedPoint) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.name = name;
        this.brandName = brandName;
        this.price = price;
        this.discountPrice = discountPrice;
        this.accumulatedPoint = accumulatedPoint;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
