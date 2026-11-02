package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderProductItemCommand(
        Long productId,
        Long sellerId,
        Long pricePolicyId,
        UUID sharerKey,
        Integer quantity,
        Long unitAmount,
        String imageUrl
) {
}
