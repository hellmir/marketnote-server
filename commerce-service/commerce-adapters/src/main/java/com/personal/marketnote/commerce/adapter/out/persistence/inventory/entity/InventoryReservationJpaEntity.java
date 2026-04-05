package com.personal.marketnote.commerce.adapter.out.persistence.inventory.entity;

import com.personal.marketnote.commerce.domain.inventory.InventoryReservation;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "inventory_reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_reservation_order_policy",
                columnNames = {"order_id", "price_policy_id"}
        )
)
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InventoryReservationJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "price_policy_id", nullable = false)
    private Long pricePolicyId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    private InventoryReservationJpaEntity(
            Long orderId,
            Long pricePolicyId,
            Integer quantity,
            LocalDateTime reservedAt
    ) {
        this.orderId = orderId;
        this.pricePolicyId = pricePolicyId;
        this.quantity = quantity;
        this.reservedAt = reservedAt;
    }

    public static InventoryReservationJpaEntity from(InventoryReservation inventoryReservation) {
        return new InventoryReservationJpaEntity(
                inventoryReservation.getOrderId(),
                inventoryReservation.getPricePolicyId(),
                inventoryReservation.getQuantity(),
                inventoryReservation.getReservedAt()
        );
    }
}
