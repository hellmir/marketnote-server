package com.personal.marketnote.commerce.domain.payment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentSnapshotState {
    private Long id;
    private Long orderId;
    private UUID orderKey;
    private String pgPaymentKey;
    private Long paymentAmount;
    private Boolean successYn;
    private Boolean refundedYn;
    private Long refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
