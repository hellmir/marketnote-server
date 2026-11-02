package com.personal.marketnote.commerce.port.in.result.payment;

import lombok.Builder;

@Builder
public record ApprovePaymentResult(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
}
