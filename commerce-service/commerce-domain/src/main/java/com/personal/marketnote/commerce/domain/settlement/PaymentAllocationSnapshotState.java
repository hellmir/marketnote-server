package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
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
    private IdempotencyKey idempotencyKey;
    private LocalDateTime createdAt;
}
