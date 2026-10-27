package com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.entity.PaymentCancelIdempotencyJpaEntity;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencySnapshotState;

public class PaymentCancelIdempotencyEntityToDomainMapper {
    private PaymentCancelIdempotencyEntityToDomainMapper() {
    }

    public static PaymentCancelIdempotency toDomain(PaymentCancelIdempotencyJpaEntity entity) {
        return PaymentCancelIdempotency.from(
                PaymentCancelIdempotencySnapshotState.builder()
                        .id(entity.getId())
                        .idempotencyKey(entity.getIdempotencyKey())
                        .paymentId(entity.getPaymentId())
                        .buyerId(entity.getBuyerId())
                        .status(entity.getStatus())
                        .result(entity.getResult())
                        .failedReason(entity.getFailedReason())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
