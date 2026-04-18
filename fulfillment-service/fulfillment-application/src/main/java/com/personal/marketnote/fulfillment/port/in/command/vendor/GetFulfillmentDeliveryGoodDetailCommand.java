package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryGoodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String orderNumber
) {
    public static GetFulfillmentDeliveryGoodDetailCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String orderNumber
    ) {
        return new GetFulfillmentDeliveryGoodDetailCommand(customerCode, accessToken, startDate, endDate, orderNumber);
    }
}
