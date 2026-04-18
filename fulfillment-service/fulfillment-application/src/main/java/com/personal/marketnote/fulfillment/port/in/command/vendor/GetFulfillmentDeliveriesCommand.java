package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveriesCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String status,
        String releaseType,
        String orderNumber
) {
    public static GetFulfillmentDeliveriesCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String releaseType
    ) {
        return new GetFulfillmentDeliveriesCommand(customerCode, accessToken, startDate, endDate, status, releaseType, null);
    }

    public static GetFulfillmentDeliveriesCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String releaseType,
            String orderNumber
    ) {
        return new GetFulfillmentDeliveriesCommand(customerCode, accessToken, startDate, endDate, status, releaseType, orderNumber);
    }
}
