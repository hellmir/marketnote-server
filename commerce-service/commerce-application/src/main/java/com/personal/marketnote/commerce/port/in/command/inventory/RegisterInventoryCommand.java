package com.personal.marketnote.commerce.port.in.command.inventory;

import java.util.UUID;

public record RegisterInventoryCommand(
        Long productId,
        UUID productKey,
        Long pricePolicyId
) {
    public static RegisterInventoryCommand of(
            Long productId,
            Long pricePolicyId
    ) {
        return new RegisterInventoryCommand(productId, null, pricePolicyId);
    }

    public static RegisterInventoryCommand of(
            Long productId,
            UUID productKey,
            Long pricePolicyId
    ) {
        return new RegisterInventoryCommand(productId, productKey, pricePolicyId);
    }
}
