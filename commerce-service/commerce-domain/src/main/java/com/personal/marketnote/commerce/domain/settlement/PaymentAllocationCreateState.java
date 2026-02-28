package com.personal.marketnote.commerce.domain.settlement;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentAllocationCreateState {
    private Long orderId;
    private Long sellerId;
    private Long allocatedAmount;
    private PaymentAllocationTransactionType transactionType;
    private PaymentAllocationTargetType targetType;
    private String idempotencyKey;
}
