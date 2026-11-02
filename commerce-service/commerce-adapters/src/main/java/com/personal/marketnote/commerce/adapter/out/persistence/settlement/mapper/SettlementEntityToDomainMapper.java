package com.personal.marketnote.commerce.adapter.out.persistence.settlement.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.SettlementJpaEntity;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;

public class SettlementEntityToDomainMapper {

    private SettlementEntityToDomainMapper() {
    }

    public static Settlement toDomain(SettlementJpaEntity entity) {
        return Settlement.from(
                SettlementSnapshotState.builder()
                        .id(entity.getId())
                        .sellerId(entity.getSellerId())
                        .year(entity.getYear())
                        .month(entity.getMonth())
                        .totalAllocatedAmount(entity.getTotalAllocatedAmount())
                        .shippingFee(entity.getShippingFee())
                        .pgFeeAmount(entity.getPgFeeAmount())
                        .platformFeeAmount(entity.getPlatformFeeAmount())
                        .sellerPayoutAmount(entity.getSellerPayoutAmount())
                        .status(entity.getStatus())
                        .version(entity.getVersion())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
