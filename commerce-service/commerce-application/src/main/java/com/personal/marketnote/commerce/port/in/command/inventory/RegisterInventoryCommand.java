package com.personal.marketnote.commerce.port.in.command.inventory;

public record RegisterInventoryCommand(
        Long productId,
        Long pricePolicyId
) {
    public static RegisterInventoryCommand of(
            Long productId,
            Long pricePolicyId
    ) {
        return new RegisterInventoryCommand(productId, pricePolicyId);
    }
}

