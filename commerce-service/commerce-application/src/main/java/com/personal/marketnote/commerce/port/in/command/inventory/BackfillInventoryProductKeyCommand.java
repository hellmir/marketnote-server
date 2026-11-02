package com.personal.marketnote.commerce.port.in.command.inventory;

import java.util.UUID;

public record BackfillInventoryProductKeyCommand(
        Long productId,
        UUID productKey
) {
    public static BackfillInventoryProductKeyCommand of(Long productId, UUID productKey) {
        return new BackfillInventoryProductKeyCommand(productId, productKey);
    }
}
