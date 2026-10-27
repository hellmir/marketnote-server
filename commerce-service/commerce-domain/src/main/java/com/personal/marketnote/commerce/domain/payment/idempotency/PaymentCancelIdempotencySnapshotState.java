package com.personal.marketnote.commerce.domain.payment.idempotency;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentCancelIdempotencySnapshotState {
    private Long id;
    private String idempotencyKey;
    private Long paymentId;
    private Long buyerId;
    private PaymentCancelIdempotencyStatus status;
    private String result;
    private String failedReason;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
