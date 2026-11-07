package com.personal.marketnote.commerce.domain.payment.idempotency;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentCancelIdempotencyCreateState {
    private String idempotencyKey;
    private Long paymentId;
    private Long buyerId;

    public static PaymentCancelIdempotencyCreateState of(String idempotencyKey, Long paymentId, Long buyerId) {
        return PaymentCancelIdempotencyCreateState.builder()
                .idempotencyKey(idempotencyKey)
                .paymentId(paymentId)
                .buyerId(buyerId)
                .build();
    }
}
