package com.personal.marketnote.commerce.port.in.result.payment;

import lombok.Builder;

@Builder
public record ReadyPaymentResult(
        String orderKey,
        String approvalKey,
        String payUrl,
        String traceNo
) {
}
