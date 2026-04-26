package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentAllocationSnapshotState {
    private Long id;
    private Long orderId;
    private Long sellerId;
    private Long allocatedAmount;
    private Long shippingFee;
    private Long settlementId;
    private PaymentAllocationTransactionType transactionType;
    private PaymentAllocationTargetType targetType;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
