package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentApprovalVendorCommand(
        String encData,
        String encInfo,
        String orderAmount,
        String orderNumber,
        String payType
) {
}
