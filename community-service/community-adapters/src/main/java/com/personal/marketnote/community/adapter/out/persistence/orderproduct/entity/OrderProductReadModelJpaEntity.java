package com.personal.marketnote.community.adapter.out.persistence.orderproduct.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "order_product_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_product_read_model_order_price_policy",
                columnNames = {"orderId", "pricePolicyId"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderProductReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long pricePolicyId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long unitAmount;

    public static OrderProductReadModelJpaEntity of(Long orderId, Long pricePolicyId, Long buyerId, Long unitAmount) {
        return OrderProductReadModelJpaEntity.builder()
                .orderId(orderId)
                .pricePolicyId(pricePolicyId)
                .buyerId(buyerId)
                .unitAmount(unitAmount)
                .build();
    }

    public void updateFrom(Long buyerId, Long unitAmount) {
        this.buyerId = buyerId;
        this.unitAmount = unitAmount;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
