package com.personal.marketnote.commerce.port.in.result.quickpayment;

import lombok.Builder;

@Builder
public record RegisterQuickPaymentTransactionResult(
        String transactionId,
        String approvalKey,
        String payUrl,
        String traceNo
) {
}
