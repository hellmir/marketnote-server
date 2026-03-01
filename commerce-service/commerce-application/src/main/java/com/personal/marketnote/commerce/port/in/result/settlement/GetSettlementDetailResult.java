package com.personal.marketnote.commerce.port.in.result.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetSettlementDetailResult(
        Long id,
        Long orderId,
        Long sellerId,
        Long allocatedAmount,
        PaymentAllocationTransactionType transactionType,
        PaymentAllocationTargetType targetType,
        LocalDateTime createdAt
) {
    public static GetSettlementDetailResult from(PaymentAllocation allocation) {
        return GetSettlementDetailResult.builder()
                .id(allocation.getId())
                .orderId(allocation.getOrderId())
                .sellerId(allocation.getSellerId())
                .allocatedAmount(allocation.getAllocatedAmount())
                .transactionType(allocation.getTransactionType())
                .targetType(allocation.getTargetType())
                .createdAt(allocation.getCreatedAt())
                .build();
    }

    public static List<GetSettlementDetailResult> fromList(List<PaymentAllocation> allocations) {
        return allocations.stream()
                .map(GetSettlementDetailResult::from)
                .toList();
    }
}
