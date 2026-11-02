package com.personal.marketnote.commerce.adapter.out.persistence.payment.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PaymentJpaEntity;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentSnapshotState;

public class PaymentEntityToDomainMapper {

    public static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.from(
                PaymentSnapshotState.builder()
                        .id(entity.getId())
                        .orderId(entity.getOrderId())
                        .orderKey(entity.getOrderKey())
                        .pgPaymentKey(entity.getPgPaymentKey())
                        .paymentAmount(entity.getPaymentAmount())
                        .successYn(entity.getSuccessYn())
                        .refundedYn(entity.getRefundedYn())
                        .refundAmount(entity.getRefundAmount())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
