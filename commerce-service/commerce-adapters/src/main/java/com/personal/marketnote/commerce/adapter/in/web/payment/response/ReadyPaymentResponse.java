package com.personal.marketnote.commerce.adapter.in.web.payment.response;

import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ReadyPaymentResponse(
        String orderKey,
        String approvalKey,
        String payUrl,
        String traceNo
) {
    public static ReadyPaymentResponse from(ReadyPaymentResult result) {
        return ReadyPaymentResponse.builder()
                .orderKey(result.orderKey())
                .approvalKey(result.approvalKey())
                .payUrl(result.payUrl())
                .traceNo(result.traceNo())
                .build();
    }
}
