package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record RegisterQuickPaymentTransactionPortCommand(
        String transactionId
) {
    public static RegisterQuickPaymentTransactionPortCommand of(String transactionId) {
        return new RegisterQuickPaymentTransactionPortCommand(transactionId);
    }
}
