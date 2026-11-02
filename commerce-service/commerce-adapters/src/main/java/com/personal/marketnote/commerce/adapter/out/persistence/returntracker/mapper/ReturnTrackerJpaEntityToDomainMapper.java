package com.personal.marketnote.commerce.adapter.out.persistence.returntracker.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.entity.ReturnTrackerJpaEntity;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTrackerSnapshotState;

public class ReturnTrackerJpaEntityToDomainMapper {

    private ReturnTrackerJpaEntityToDomainMapper() {
    }

    public static ReturnTracker toDomain(ReturnTrackerJpaEntity entity) {
        return ReturnTracker.from(
                ReturnTrackerSnapshotState.builder()
                        .id(entity.getId())
                        .orderId(entity.getOrderId())
                        .returnSlipNumber(entity.getReturnSlipNumber())
                        .inspectionStatus(entity.getInspectionStatus())
                        .refundStatus(entity.getRefundStatus())
                        .inspectedAt(entity.getInspectedAt())
                        .refundedAt(entity.getRefundedAt())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
