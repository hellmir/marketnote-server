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
public class InventoryDeductionHistories {
    private List<InventoryDeductionHistory> inventoryDeductionHistories;

    public static InventoryDeductionHistories from(
            Map<Long, Integer> stocksByPricePolicyId,
            Map<Long, Long> productIdsByPricePolicyId,
            Long orderId,
            String reason
    ) {
        return new InventoryDeductionHistories(
                stocksByPricePolicyId.entrySet()
                        .stream()
                        .map(entry -> InventoryDeductionHistory.from(
                                InventoryDeductionHistoryCreateState.builder()
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
