package com.personal.marketnote.commerce.domain.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class InventoryRestorationHistories {
    private List<InventoryRestorationHistory> inventoryRestorationHistories;

    public static InventoryRestorationHistories from(
            Map<Long, Integer> stocksByPricePolicyId,
            Map<Long, Long> productIdsByPricePolicyId,
            Long orderId,
            String reason
    ) {
        return new InventoryRestorationHistories(
                stocksByPricePolicyId.entrySet()
                        .stream()
                        .map(entry -> InventoryRestorationHistory.from(
                                InventoryRestorationHistoryCreateState.builder()
                                        .productId(productIdsByPricePolicyId.get(entry.getKey()))
                                        .pricePolicyId(entry.getKey())
                                        .orderId(orderId)
                                        .stock(entry.getValue())
                                        .reason(reason)
                                        .build()
                        ))
                        .toList()
        );
    }
}
