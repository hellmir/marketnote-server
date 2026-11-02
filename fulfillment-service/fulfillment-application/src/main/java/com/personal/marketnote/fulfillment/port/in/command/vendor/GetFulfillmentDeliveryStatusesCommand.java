package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryStatusesCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String releaseType
) {
    public static GetFulfillmentDeliveryStatusesCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String releaseType
    ) {
        return new GetFulfillmentDeliveryStatusesCommand(customerCode, accessToken, startDate, endDate, releaseType);
    }
}
