package com.personal.marketnote.commerce.adapter.in.web.settlement.response;

import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetSettlementResponse(
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
    public static GetSettlementResponse from(GetSettlementResult result) {
        return GetSettlementResponse.builder()
                .id(result.id())
                .sellerId(result.sellerId())
                .year(result.year())
                .month(result.month())
                .totalAllocatedAmount(result.totalAllocatedAmount())
                .pgFeeAmount(result.pgFeeAmount())
                .platformFeeAmount(result.platformFeeAmount())
                .sellerPayoutAmount(result.sellerPayoutAmount())
                .status(result.status())
                .createdAt(result.createdAt())
                .modifiedAt(result.modifiedAt())
                .build();
    }
}
