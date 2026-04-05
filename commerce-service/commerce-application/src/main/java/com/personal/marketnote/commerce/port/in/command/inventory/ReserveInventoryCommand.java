package com.personal.marketnote.commerce.port.in.command.inventory;

import lombok.Builder;

import java.util.List;

@Builder
public record ReserveInventoryCommand(
        Long orderId,
        List<OrderProductItem> orderProducts
) {
    @Builder
    public record OrderProductItem(
            Long pricePolicyId,
            int quantity
    ) {
    }
}
