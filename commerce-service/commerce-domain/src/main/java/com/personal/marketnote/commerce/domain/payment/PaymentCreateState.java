package com.personal.marketnote.commerce.domain.payment;

import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentCreateState {
    private Long orderId;
    private UUID orderKey;
    private Long paymentAmount;
}
