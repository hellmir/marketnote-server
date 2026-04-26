package com.personal.marketnote.commerce.adapter.in.web.settlement.response;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetSettlementDetailResponse(
        Long id,
        Long orderId,
        Long sellerId,
        Long allocatedAmount,
        Long shippingFee,
        PaymentAllocationTransactionType transactionType,
        PaymentAllocationTargetType targetType,
        LocalDateTime createdAt
) {
    public static GetSettlementDetailResponse from(GetSettlementDetailResult result) {
        return GetSettlementDetailResponse.builder()
                .id(result.id())
                .orderId(result.orderId())
                .sellerId(result.sellerId())
                .allocatedAmount(result.allocatedAmount())
                .shippingFee(result.shippingFee())
                .transactionType(result.transactionType())
                .targetType(result.targetType())
                .createdAt(result.createdAt())
                .build();
    }

    public static List<GetSettlementDetailResponse> fromList(List<GetSettlementDetailResult> results) {
        return results.stream()
                .map(GetSettlementDetailResponse::from)
                .toList();
    }
}
