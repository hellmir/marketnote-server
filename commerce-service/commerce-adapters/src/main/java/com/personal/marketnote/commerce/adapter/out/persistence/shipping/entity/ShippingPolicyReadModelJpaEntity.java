package com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "shipping_policy_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shipping_policy_read_model_seller_id",
                columnNames = "seller_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingPolicyReadModelJpaEntity extends BaseGeneralEntity {

    @Column(name = "seller_id", nullable = false, unique = true)
    private Long sellerId;

    @Column(name = "shipping_fee", nullable = false)
    private Long shippingFee;

    @Column(name = "free_shipping_threshold", nullable = false)
    private Long freeShippingThreshold;

    public static ShippingPolicyReadModelJpaEntity of(Long sellerId, Long shippingFee, Long freeShippingThreshold) {
        return ShippingPolicyReadModelJpaEntity.builder()
                .sellerId(sellerId)
                .shippingFee(shippingFee)
                .freeShippingThreshold(freeShippingThreshold)
                .build();
    }

    public void updateFrom(Long shippingFee, Long freeShippingThreshold) {
        this.shippingFee = shippingFee;
        this.freeShippingThreshold = freeShippingThreshold;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
