package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record CancelFulfillmentDeliveryItemCommand(
        String slipNumber,
        String orderNumber
) {
    public static CancelFulfillmentDeliveryItemCommand of(
            String slipNumber,
            String orderNumber
    ) {
        return new CancelFulfillmentDeliveryItemCommand(slipNumber, orderNumber);
    }
}
