package com.personal.marketnote.commerce.adapter.in.web.payment.response;

import com.personal.marketnote.commerce.port.in.result.payment.GetPaymentResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetPaymentResponse(
        Long orderId,
        String orderKey,
        Long paymentAmount,
        Boolean successYn,
        Boolean refundedYn,
        Long refundAmount,
        String pgPaymentKey,
        String pgCompanyKey,
        String method,
        String cardNumber,
        String approvalNumber,
        Short installment,
        String issueCompanyName,
        String resultCode,
        String resultMessage
) {
    public static GetPaymentResponse from(GetPaymentResult result) {
        return GetPaymentResponse.builder()
                .orderId(result.orderId())
                .orderKey(result.orderKey())
                .paymentAmount(result.paymentAmount())
                .successYn(result.successYn())
                .refundedYn(result.refundedYn())
                .refundAmount(result.refundAmount())
                .pgPaymentKey(result.pgPaymentKey())
                .pgCompanyKey(result.pgCompanyKey())
                .method(result.method())
                .cardNumber(result.cardNumber())
                .approvalNumber(result.approvalNumber())
                .installment(result.installment())
                .issueCompanyName(result.issueCompanyName())
                .resultCode(result.resultCode())
                .resultMessage(result.resultMessage())
                .build();
    }
}
