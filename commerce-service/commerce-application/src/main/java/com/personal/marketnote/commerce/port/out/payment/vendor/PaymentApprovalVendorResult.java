package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentApprovalVendorResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String resultEnMessage,
        String transactionId,
        String amount,
        String payMethod,
        String cardCode,
        String cardName,
        String cardNumber,
        String approvalNumber,
        String approvalTime,
        String installmentInfo,
        String installmentType,
        String installmentMonths,
        String cardAmount,
        String couponAmount,
        String partialCancelYn,
        String cardBinType01,
        String cardBinType02,
        String rawResponse
) {
}
