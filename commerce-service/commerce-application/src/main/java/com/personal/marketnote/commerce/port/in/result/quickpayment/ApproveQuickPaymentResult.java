package com.personal.marketnote.commerce.port.in.result.quickpayment;

import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import lombok.Builder;

@Builder
public record ApproveQuickPaymentResult(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
    public static ApproveQuickPaymentResult from(ApprovePaymentResult paymentResult) {
        return new ApproveQuickPaymentResult(
                paymentResult.orderId(),
                paymentResult.orderKey(),
                paymentResult.pgPaymentKey(),
                paymentResult.amount(),
                paymentResult.resultCode(),
                paymentResult.resultMessage(),
                paymentResult.payMethod()
        );
    }
}
