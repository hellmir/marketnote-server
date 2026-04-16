package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record ApproveQuickPaymentPortResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String transactionId,
        String amount,
        String payMethod,
        String cardCode,
        String cardName,
        String cardNumber,
        String approvalNumber,
        String approvalTime,
        String installmentMonths,
        String cardAmount,
        String partialCancelYn,
        String cardBinType01,
        String cardBinType02,
        String rawResponse
) {
}
