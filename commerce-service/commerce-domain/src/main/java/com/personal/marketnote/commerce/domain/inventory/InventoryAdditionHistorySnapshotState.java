package com.personal.marketnote.commerce.domain.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAdditionHistorySnapshotState {
    private final Long id;
    private final Long productId;
    private final Long pricePolicyId;
    private final Integer stock;
    private final String reason;
    private final Long unitPrice;
    private final String supplier;
}
