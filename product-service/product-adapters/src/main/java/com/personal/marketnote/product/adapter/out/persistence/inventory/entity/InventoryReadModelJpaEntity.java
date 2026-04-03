package com.personal.marketnote.product.adapter.out.persistence.inventory.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "inventory_read_models",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_read_model_price_policy_id", columnNames = "pricePolicyId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class InventoryReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private Long pricePolicyId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer stockQuantity;

    public static InventoryReadModelJpaEntity of(Long pricePolicyId, Long productId, Integer stockQuantity) {
        return InventoryReadModelJpaEntity.builder()
                .pricePolicyId(pricePolicyId)
                .productId(productId)
                .stockQuantity(stockQuantity)
                .build();
    }

    public void updateFrom(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
