package com.personal.marketnote.commerce.port.in.result.order;

import lombok.Builder;

@Builder
public record CalculateReturnShippingFeeResult(
        long returnShippingFee
) {
}
