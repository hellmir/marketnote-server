package com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.PaymentAllocationJpaEntity;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationSnapshotState;

public class PaymentAllocationEntityToDomainMapper {

    private PaymentAllocationEntityToDomainMapper() {
    }

    public static PaymentAllocation toDomain(PaymentAllocationJpaEntity entity) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .sellerId(entity.getSellerId())
                .allocatedAmount(entity.getAllocatedAmount())
                .settlementId(entity.getSettlementId())
                .transactionType(entity.getTransactionType())
                .targetType(entity.getTargetType())
                .idempotencyKey(entity.getIdempotencyKey())
                .createdAt(entity.getCreatedAt())
                .build());
    }
}
