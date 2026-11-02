package com.personal.marketnote.product.port.in.command;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record OrderingItemQuery(
        Long pricePolicyId,
        UUID sharerKey,
        Short quantity,
        String imageUrl
) {
    public static OrderingItemQuery of(Long pricePolicyId, UUID sharerKey, Short quantity, String imageUrl) {
        return OrderingItemQuery.builder()
                .pricePolicyId(pricePolicyId)
                .sharerKey(sharerKey)
                .quantity(quantity)
                .imageUrl(imageUrl)
                .build();
    }
}
