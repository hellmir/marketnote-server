package com.personal.marketnote.product.adapter.out.persistence.shipping.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "shipping_policy", indexes = {
        @Index(name = "idx_shipping_policy_seller_id", columnList = "seller_id", unique = true)
})
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingPolicyJpaEntity extends BaseGeneralEntity {

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "delivery_company", nullable = false, length = 50)
    private String deliveryCompany;

    @Column(name = "shipping_fee", nullable = false)
    private Long shippingFee;

    @Column(name = "free_shipping_threshold", nullable = false)
    private Long freeShippingThreshold;

    @Column(name = "jeju_surcharge")
    private Long jejuSurcharge;

    @Column(name = "island_surcharge")
    private Long islandSurcharge;

    public static ShippingPolicyJpaEntity from(ShippingPolicy policy) {
        return ShippingPolicyJpaEntity.builder()
                .sellerId(policy.getSellerId())
                .deliveryCompany(policy.getDeliveryCompany())
                .shippingFee(policy.getShippingFee())
                .freeShippingThreshold(policy.getFreeShippingThreshold())
                .jejuSurcharge(policy.getJejuSurcharge())
                .islandSurcharge(policy.getIslandSurcharge())
                .build();
    }

    public void updateFrom(ShippingPolicy policy) {
        this.deliveryCompany = policy.getDeliveryCompany();
        this.shippingFee = policy.getShippingFee();
        this.freeShippingThreshold = policy.getFreeShippingThreshold();
        this.jejuSurcharge = policy.getJejuSurcharge();
        this.islandSurcharge = policy.getIslandSurcharge();
    }
}
