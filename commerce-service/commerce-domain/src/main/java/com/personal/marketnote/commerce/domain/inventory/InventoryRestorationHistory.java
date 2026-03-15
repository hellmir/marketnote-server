package com.personal.marketnote.commerce.domain.inventory;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class InventoryRestorationHistory {
    private Long id;
    private Long productId;
    private Long pricePolicyId;
    private Long orderId;
    private Stock stock;
    private String reason;

    public static InventoryRestorationHistory from(InventoryRestorationHistoryCreateState state) {
        return InventoryRestorationHistory.builder()
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .orderId(state.getOrderId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .reason(state.getReason())
                .build();
    }

    public static InventoryRestorationHistory from(InventoryRestorationHistorySnapshotState state) {
        return InventoryRestorationHistory.builder()
                .id(state.getId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .orderId(state.getOrderId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .reason(state.getReason())
                .build();
    }

    public Integer getStockValue() {
        return stock.getValue();
    }
}
