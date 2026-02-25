package com.personal.marketnote.commerce.adapter.out.persistence.payment.entity;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(columnNames = "order_key"))
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PaymentJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_key", nullable = false)
    private UUID orderKey;

    @Column(name = "pg_payment_key")
    private String pgPaymentKey;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    @Column(name = "success_yn")
    private Boolean successYn;

    @Column(name = "refunded_yn", nullable = false)
    private Boolean refundedYn;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long version;

    public static PaymentJpaEntity from(Payment payment) {
        return PaymentJpaEntity.builder()
                .orderId(payment.getOrderId())
                .orderKey(payment.getOrderKey())
                .pgPaymentKey(payment.getPgPaymentKey())
                .paymentAmount(payment.getPaymentAmount())
                .successYn(payment.getSuccessYn())
                .refundedYn(payment.getRefundedYn())
                .refundAmount(payment.getRefundAmount())
                .build();
    }

    public void updateFrom(Payment payment) {
        this.pgPaymentKey = payment.getPgPaymentKey();
        this.successYn = payment.getSuccessYn();
        this.refundedYn = payment.getRefundedYn();
        this.refundAmount = payment.getRefundAmount();
    }

    @PostLoad
    private void initVersionAfterLoad() {
        if (FormatValidator.hasNoValue(version)) {
            version = 0L;
        }
    }

    @PrePersist
    private void initVersionBeforePersist() {
        if (FormatValidator.hasNoValue(version)) {
            version = 0L;
        }
    }
}
