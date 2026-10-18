package com.personal.marketnote.fulfillment.port.in.command;

import java.util.UUID;

public record RegisterInternalReturnDeliveryProductCommand(
        UUID productKey,
        Integer quantity
) {
    public static RegisterInternalReturnDeliveryProductCommand of(UUID productKey, Integer quantity) {
        return new RegisterInternalReturnDeliveryProductCommand(productKey, quantity);
    }
}
