package com.personal.marketnote.product.port.in.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AddCartProductCommand(
        Long userId,
        UUID sharerKey,
        Long pricePolicyId,
        String imageUrl,
        Short quantity
) {
}
