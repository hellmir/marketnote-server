package com.personal.marketnote.commerce.adapter.out.persistence.product.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "product_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_read_model_price_policy_id",
                columnNames = "price_policy_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductReadModelJpaEntity extends BaseGeneralEntity {

    @Column(name = "price_policy_id", nullable = false, unique = true)
    private Long pricePolicyId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "price")
    private Long price;

    @Column(name = "discount_price")
    private Long discountPrice;

    @Column(name = "accumulated_point")
    private Long accumulatedPoint;

    public static ProductReadModelJpaEntity of(
            Long pricePolicyId, Long productId, Long sellerId,
            String name, String brandName,
            Long price, Long discountPrice, Long accumulatedPoint
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

    public void updateFrom(
            String name, String brandName,
            Long price, Long discountPrice, Long accumulatedPoint
    ) {
        this.name = name;
        this.brandName = brandName;
        this.price = price;
        this.discountPrice = discountPrice;
        this.accumulatedPoint = accumulatedPoint;
        activate();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void markInactive() {
        deactivate();
    }
}
