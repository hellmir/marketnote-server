package com.personal.marketnote.commerce.adapter.in.web.quickpayment.response;

import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record RegisterQuickPaymentTransactionResponse(
        String transactionId,
        String approvalKey,
        String payUrl,
        String traceNo
) {
    public static RegisterQuickPaymentTransactionResponse from(RegisterQuickPaymentTransactionResult result) {
        return RegisterQuickPaymentTransactionResponse.builder()
                .transactionId(result.transactionId())
                .approvalKey(result.approvalKey())
                .payUrl(result.payUrl())
                .traceNo(result.traceNo())
                .build();
    }
}
