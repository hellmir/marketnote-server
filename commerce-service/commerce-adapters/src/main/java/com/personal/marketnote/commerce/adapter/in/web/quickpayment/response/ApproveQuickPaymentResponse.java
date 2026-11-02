package com.personal.marketnote.commerce.adapter.in.web.quickpayment.response;

import com.personal.marketnote.commerce.port.in.result.quickpayment.ApproveQuickPaymentResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ApproveQuickPaymentResponse(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
    public static ApproveQuickPaymentResponse from(ApproveQuickPaymentResult result) {
        return ApproveQuickPaymentResponse.builder()
                .orderId(result.orderId())
                .orderKey(result.orderKey())
                .pgPaymentKey(result.pgPaymentKey())
                .amount(result.amount())
                .resultCode(result.resultCode())
                .resultMessage(result.resultMessage())
                .payMethod(result.payMethod())
                .build();
    }
}
