package com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity;

import com.personal.marketnote.commerce.domain.inventory.InventoryDeductionHistory;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "inventory_deduction_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_deduction_order_policy",
                columnNames = {"order_id", "price_policy_id"}
        )
)
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InventoryDeductionHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "price_policy_id", nullable = false)
    private Long pricePolicyId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "reason", length = 511)
    private String reason;

    private InventoryDeductionHistoryJpaEntity(
            Long productId,
            Long pricePolicyId,
            Long orderId,
            Integer stock,
            String reason
    ) {
        this.productId = productId;
        this.pricePolicyId = pricePolicyId;
        this.orderId = orderId;
        this.stock = stock;
        this.reason = reason;
    }

    public static InventoryDeductionHistoryJpaEntity from(InventoryDeductionHistory inventoryDeductionHistory) {
        return new InventoryDeductionHistoryJpaEntity(
                inventoryDeductionHistory.getProductId(),
                inventoryDeductionHistory.getPricePolicyId(),
                inventoryDeductionHistory.getOrderId(),
                inventoryDeductionHistory.getStockValue(),
                inventoryDeductionHistory.getReason()
        );
    }
}
