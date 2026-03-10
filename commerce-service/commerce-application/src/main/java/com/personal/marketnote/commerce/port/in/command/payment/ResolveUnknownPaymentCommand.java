package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record ResolveUnknownPaymentCommand(
        String orderKey,
        String resolvedStatus,
        String resultCode,
        String resultMessage,
        String pgPaymentKey,
        String approvalNumber,
        String appTime
) {
}
