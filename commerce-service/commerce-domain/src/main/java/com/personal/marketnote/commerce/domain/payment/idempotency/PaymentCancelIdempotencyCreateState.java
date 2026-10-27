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
}
