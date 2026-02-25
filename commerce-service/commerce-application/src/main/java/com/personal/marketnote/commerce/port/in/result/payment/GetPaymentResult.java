package com.personal.marketnote.commerce.port.in.result.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

@Builder
public record GetPaymentResult(
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
    public static GetPaymentResult of(Payment payment, PspPaymentEvent event) {
        GetPaymentResultBuilder builder = GetPaymentResult.builder()
                .orderId(payment.getOrderId())
                .orderKey(payment.getOrderKey().toString())
                .paymentAmount(payment.getPaymentAmount())
                .successYn(payment.getSuccessYn())
                .refundedYn(payment.getRefundedYn())
                .refundAmount(payment.getRefundAmount())
                .pgPaymentKey(payment.getPgPaymentKey());

        if (FormatValidator.hasValue(event)) {
            builder.pgCompanyKey(event.getPgCompanyKey())
                    .method(event.getMethod())
                    .cardNumber(event.getCardNumber())
                    .approvalNumber(event.getApprovalNumber())
                    .installment(event.getInstallment())
                    .issueCompanyName(event.getIssueCompanyName())
                    .resultCode(event.getResultCode())
                    .resultMessage(event.getResultMessage());
        }

        return builder.build();
    }
}
