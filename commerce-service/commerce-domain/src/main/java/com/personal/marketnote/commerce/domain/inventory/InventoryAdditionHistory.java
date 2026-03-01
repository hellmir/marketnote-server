package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryAdditionHistoryCreateState;
import com.personal.marketnote.commerce.domain.inventory.InventoryAdditionHistorySnapshotState;
import com.personal.marketnote.commerce.domain.inventory.Stock;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class InventoryAdditionHistory {
    private Long id;
    private Long productId;
    private Long pricePolicyId;
    private Stock stock;
    private String reason;
    private Long unitPrice;
    private String supplier;

    public static InventoryAdditionHistory from(InventoryAdditionHistoryCreateState state) {
        return InventoryAdditionHistory.builder()
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .reason(state.getReason())
                .unitPrice(state.getUnitPrice())
                .supplier(state.getSupplier())
                .build();
    }

    public static InventoryAdditionHistory from(InventoryAdditionHistorySnapshotState state) {
        return InventoryAdditionHistory.builder()
                .id(state.getId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .reason(state.getReason())
                .unitPrice(state.getUnitPrice())
                .supplier(state.getSupplier())
                .build();
    }
}
