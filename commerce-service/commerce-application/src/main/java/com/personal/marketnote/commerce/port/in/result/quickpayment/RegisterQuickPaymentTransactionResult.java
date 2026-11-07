package com.personal.marketnote.commerce.port.in.result.quickpayment;

import lombok.Builder;

@Builder
public record RegisterQuickPaymentTransactionResult(
        String transactionId,
        String approvalKey,
        String payUrl,
        String traceNo
) {
    public static RegisterQuickPaymentTransactionResult of(
            String transactionId, String approvalKey, String payUrl, String traceNo
    ) {
        return new RegisterQuickPaymentTransactionResult(transactionId, approvalKey, payUrl, traceNo);
    }
}
