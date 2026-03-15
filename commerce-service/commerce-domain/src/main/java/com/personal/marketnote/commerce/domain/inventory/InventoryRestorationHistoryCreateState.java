package com.personal.marketnote.commerce.domain.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryRestorationHistoryCreateState {
    private final Long productId;
    private final Long pricePolicyId;
    private final Long orderId;
    private final Integer stock;
    private final String reason;
}
