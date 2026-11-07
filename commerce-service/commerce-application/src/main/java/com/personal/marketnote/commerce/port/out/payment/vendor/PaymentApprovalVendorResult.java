package com.personal.marketnote.commerce.port.out.payment.vendor;

import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortResult;
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
    public static PaymentApprovalVendorResult from(ApproveQuickPaymentPortResult portResult) {
        return PaymentApprovalVendorResult.builder()
                .success(portResult.success())
                .resultCode(portResult.resultCode())
                .resultMessage(portResult.resultMessage())
                .transactionId(portResult.transactionId())
                .amount(portResult.amount())
                .payMethod(portResult.payMethod())
                .cardCode(portResult.cardCode())
                .cardName(portResult.cardName())
                .cardNumber(portResult.cardNumber())
                .approvalNumber(portResult.approvalNumber())
                .approvalTime(portResult.approvalTime())
                .installmentMonths(portResult.installmentMonths())
                .cardAmount(portResult.cardAmount())
                .partialCancelYn(portResult.partialCancelYn())
                .cardBinType01(portResult.cardBinType01())
                .cardBinType02(portResult.cardBinType02())
                .rawResponse(portResult.rawResponse())
                .build();
    }
}
