package com.personal.marketnote.commerce.adapter.out.persistence.refund.entity;

import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 환불 JPA 엔티티.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Entity
@Table(name = "refund",
        indexes = {
                @Index(name = "idx_refund_order_id", columnList = "order_id"),
                @Index(name = "idx_refund_payment_id", columnList = "payment_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RefundJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "processed_by", length = 100)
    private String processedBy;

    @Column(name = "pg_refund_key", length = 200)
    private String pgRefundKey;

    @Column(name = "pg_raw_response", columnDefinition = "TEXT")
    private String pgRawResponse;

    public static RefundJpaEntity from(Refund refund) {
        return RefundJpaEntity.builder()
                .paymentId(refund.getPaymentId())
                .orderId(refund.getOrderId())
                .refundType(refund.getRefundType())
                .refundAmount(refund.getRefundAmount())
                .cancelReason(refund.getCancelReason())
                .processedBy(refund.getProcessedBy())
                .pgRefundKey(refund.getPgRefundKey())
                .pgRawResponse(refund.getPgRawResponse())
                .build();
    }
}
