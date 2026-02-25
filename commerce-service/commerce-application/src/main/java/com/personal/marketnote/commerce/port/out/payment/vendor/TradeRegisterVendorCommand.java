package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record TradeRegisterVendorCommand(
        String orderKey,
        String goodMny,
        String payMethod,
        String goodName
) {
}
