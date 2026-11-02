package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record RegisterQuickPaymentTransactionPortResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String approvalKey,
        String payUrl,
        String traceNo,
        String rawResponse
) {
}
