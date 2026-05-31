package com.personal.marketnote.fulfillment.port.in.command;

public record RegisterInternalReturnDeliveryProductCommand(
        String productCode,
        Integer quantity
) {
    public static RegisterInternalReturnDeliveryProductCommand of(String productCode, Integer quantity) {
        return new RegisterInternalReturnDeliveryProductCommand(productCode, quantity);
    }
}
