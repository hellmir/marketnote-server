package com.personal.marketnote.commerce.port.in.result.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import lombok.Builder;

@Builder
public record ApprovePaymentResult(
        Long orderId,
        String orderKey,
        String pgPaymentKey,
        Long amount,
        String resultCode,
        String resultMessage,
        String payMethod
) {
    public static ApprovePaymentResult of(Payment payment, PaymentApprovalVendorResult vendorResult) {
        return new ApprovePaymentResult(
                payment.getOrderId(),
                payment.getOrderKey().toString(),
                vendorResult.transactionId(),
                payment.getPaymentAmount().getValue(),
                vendorResult.resultCode(),
                vendorResult.resultMessage(),
                vendorResult.payMethod()
        );
    }
}
