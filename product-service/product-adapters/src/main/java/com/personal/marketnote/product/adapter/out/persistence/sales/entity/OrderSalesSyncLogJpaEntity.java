package com.personal.marketnote.product.adapter.out.persistence.sales.entity;

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
        name = "order_sales_sync_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_sales_sync_log_order_id",
                columnNames = "order_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderSalesSyncLogJpaEntity extends BaseGeneralEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    public static OrderSalesSyncLogJpaEntity of(Long orderId) {
        return OrderSalesSyncLogJpaEntity.builder()
                .orderId(orderId)
                .build();
    }
}
