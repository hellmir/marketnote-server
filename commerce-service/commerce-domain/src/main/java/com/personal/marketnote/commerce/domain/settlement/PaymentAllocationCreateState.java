package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentAllocationCreateState {
    private Long orderId;
    private Long sellerId;
    private Long allocatedAmount;
    private Long shippingFee;
    private PaymentAllocationTransactionType transactionType;
    private PaymentAllocationTargetType targetType;
    private IdempotencyKey idempotencyKey;
}
