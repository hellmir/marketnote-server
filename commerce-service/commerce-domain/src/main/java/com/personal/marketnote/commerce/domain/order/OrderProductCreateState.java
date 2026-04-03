package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProductCreateState {
    private final Long sellerId;
    private final Long pricePolicyId;
    private final UUID sharerKey;
    private final Integer quantity;
    private final Long unitAmount;
    private final String imageUrl;
}

