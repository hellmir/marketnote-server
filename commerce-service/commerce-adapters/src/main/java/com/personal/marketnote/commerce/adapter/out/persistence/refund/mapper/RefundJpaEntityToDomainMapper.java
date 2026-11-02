package com.personal.marketnote.commerce.adapter.out.persistence.refund.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.refund.entity.RefundJpaEntity;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundSnapshotState;

/**
 * 환불 JPA 엔티티 → 도메인 매퍼.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public class RefundJpaEntityToDomainMapper {

    private RefundJpaEntityToDomainMapper() {
    }

    public static Refund toDomain(RefundJpaEntity entity) {
        return Refund.from(RefundSnapshotState.builder()
                .id(entity.getId())
                .paymentId(entity.getPaymentId())
                .orderId(entity.getOrderId())
                .refundType(entity.getRefundType())
                .refundAmount(entity.getRefundAmount())
                .cancelReason(entity.getCancelReason())
                .processedBy(entity.getProcessedBy())
                .pgRefundKey(entity.getPgRefundKey())
                .pgRawResponse(entity.getPgRawResponse())
                .createdAt(entity.getCreatedAt())
                .build());
    }
}
