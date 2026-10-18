package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record CancelFulfillmentDeliveryItemCommand(
        String slipNumber,
        String orderNumber,
        String productKey
) {
    public static CancelFulfillmentDeliveryItemCommand of(
            String slipNumber,
            String orderNumber
    ) {
        return new CancelFulfillmentDeliveryItemCommand(slipNumber, orderNumber, null);
    }

    public static CancelFulfillmentDeliveryItemCommand of(
            String slipNumber,
            String orderNumber,
            String productKey
    ) {
        return new CancelFulfillmentDeliveryItemCommand(slipNumber, orderNumber, productKey);
    }
}
