package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

@Builder
public record OrderAmountCommand(
        Long totalAmount,
        Long couponAmount,
        Long pointAmount,
        Long shippingFee
) {
}
