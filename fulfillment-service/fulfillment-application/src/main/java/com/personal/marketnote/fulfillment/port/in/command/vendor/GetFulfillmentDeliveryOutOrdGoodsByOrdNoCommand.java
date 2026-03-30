package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String ordNo
) {
    public static GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String ordNo
    ) {
        return new GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand(customerCode, accessToken, startDate, endDate, ordNo);
    }
}
