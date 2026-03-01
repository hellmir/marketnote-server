package com.personal.marketnote.commerce.port.in.result.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetSettlementResult(
        Long id,
        Long sellerId,
        Integer year,
        Integer month,
        Long totalAllocatedAmount,
        Long pgFeeAmount,
        Long platformFeeAmount,
        Long sellerPayoutAmount,
        SettlementStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static GetSettlementResult from(Settlement settlement) {
        return GetSettlementResult.builder()
                .id(settlement.getId())
                .sellerId(settlement.getSellerId())
                .year(settlement.getYear())
                .month(settlement.getMonth())
                .totalAllocatedAmount(settlement.getTotalAllocatedAmount())
                .pgFeeAmount(settlement.getPgFeeAmount())
                .platformFeeAmount(settlement.getPlatformFeeAmount())
                .sellerPayoutAmount(settlement.getSellerPayoutAmount())
                .status(settlement.getStatus())
                .createdAt(settlement.getCreatedAt())
                .modifiedAt(settlement.getModifiedAt())
                .build();
    }
}
