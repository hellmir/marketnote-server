package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
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
    private Money unitPrice;
    private String supplier;

    public static InventoryAdditionHistory from(InventoryAdditionHistoryCreateState state) {
        return InventoryAdditionHistory.builder()
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .stock(Stock.of(
                        String.valueOf(state.getStock())
                ))
                .reason(state.getReason())
                .unitPrice(resolveMoneyOrZero(state.getUnitPrice()))
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
                .unitPrice(resolveMoneyOrZero(state.getUnitPrice()))
                .supplier(state.getSupplier())
                .build();
    }

    private static Money resolveMoneyOrZero(Long value) {
        if (FormatValidator.hasNoValue(value)) {
            return Money.zero();
        }
        return Money.of(value);
    }
}
