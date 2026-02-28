package com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_allocation",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PaymentAllocationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "allocated_amount", nullable = false)
    private Long allocatedAmount;

    @Column(name = "settlement_id")
    private Long settlementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 31)
    private PaymentAllocationTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 15)
    private PaymentAllocationTargetType targetType;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static PaymentAllocationJpaEntity from(PaymentAllocation allocation) {
        return PaymentAllocationJpaEntity.builder()
                .orderId(allocation.getOrderId())
                .sellerId(allocation.getSellerId())
                .allocatedAmount(allocation.getAllocatedAmount())
                .settlementId(allocation.getSettlementId())
                .transactionType(allocation.getTransactionType())
                .targetType(allocation.getTargetType())
                .idempotencyKey(allocation.getIdempotencyKey())
                .build();
    }
}
