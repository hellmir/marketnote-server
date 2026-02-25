package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentCancelVendorCommand(
        String tno,
        String modType,
        Long modMny,
        Long remMny,
        String modDesc
) {
}
