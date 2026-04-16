package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record ApproveQuickPaymentPortCommand(
        String batchKey,
        String groupId,
        String orderKey,
        String amount,
        String goodName
) {
}
