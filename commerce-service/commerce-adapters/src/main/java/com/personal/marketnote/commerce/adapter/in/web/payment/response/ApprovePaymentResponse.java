package com.personal.marketnote.commerce.adapter.in.web.payment.response;

import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ApprovePaymentResponse(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
    public static ApprovePaymentResponse from(ApprovePaymentResult result) {
        return ApprovePaymentResponse.builder()
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
