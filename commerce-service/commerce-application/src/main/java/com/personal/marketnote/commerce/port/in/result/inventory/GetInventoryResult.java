package com.personal.marketnote.commerce.port.in.result.inventory;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetInventoryResult(
        Long pricePolicyId,
        Integer stock
) {
    public static GetInventoryResult from(Inventory inventory) {
        return GetInventoryResult.builder()
                .pricePolicyId(inventory.getPricePolicyId())
                .stock(inventory.getStockValue())
                .build();
    }
}
