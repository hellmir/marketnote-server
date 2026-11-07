package com.personal.marketnote.commerce.port.in.result.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
import lombok.Builder;

@Builder
public record ReadyPaymentResult(
        String orderKey,
        String approvalKey,
        String payUrl,
        String traceNo
) {
    public static ReadyPaymentResult of(Payment payment, TradeRegisterVendorResult vendorResult) {
        return new ReadyPaymentResult(
                payment.getOrderKey().toString(),
                vendorResult.approvalKey(),
                vendorResult.payUrl(),
                vendorResult.traceNo()
        );
    }
}
