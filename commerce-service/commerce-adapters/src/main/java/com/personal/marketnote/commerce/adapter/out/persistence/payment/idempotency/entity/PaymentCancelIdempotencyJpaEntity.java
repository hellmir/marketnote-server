package com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.entity;

import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyStatus;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "payment_cancel_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_cancel_idempotency_key",
                columnNames = "idempotency_key"
        ),
        indexes = @Index(
                name = "idx_payment_cancel_idempotency_created_at",
                columnList = "created_at"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PaymentCancelIdempotencyJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 31)
    private PaymentCancelIdempotencyStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    private String result;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason;

    public static PaymentCancelIdempotencyJpaEntity from(PaymentCancelIdempotency domain) {
        return PaymentCancelIdempotencyJpaEntity.builder()
                .id(domain.getId())
                .idempotencyKey(domain.getIdempotencyKey())
                .paymentId(domain.getPaymentId())
                .buyerId(domain.getBuyerId())
                .status(domain.getStatus())
                .result(domain.getResult())
                .failedReason(domain.getFailedReason())
                .build();
    }

    public void updateFrom(PaymentCancelIdempotency domain) {
        status = domain.getStatus();
        result = domain.getResult();
        failedReason = domain.getFailedReason();
    }
}
