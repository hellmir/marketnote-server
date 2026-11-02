package com.personal.marketnote.commerce.port.in.result.quickpayment;

import lombok.Builder;

@Builder
public record ApproveQuickPaymentResult(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
}
