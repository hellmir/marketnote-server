package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryDetailCommand(
        String customerCode,
        String accessToken,
        String slipNumber,
        String orderNumber
) {
    public static GetFulfillmentDeliveryDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNumber
    ) {
        return new GetFulfillmentDeliveryDetailCommand(customerCode, accessToken, slipNumber, null);
    }

    public static GetFulfillmentDeliveryDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNumber,
            String orderNumber
    ) {
        return new GetFulfillmentDeliveryDetailCommand(customerCode, accessToken, slipNumber, orderNumber);
    }
}
