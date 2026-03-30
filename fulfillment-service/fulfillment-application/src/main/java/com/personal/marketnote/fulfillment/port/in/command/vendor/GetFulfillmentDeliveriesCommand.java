package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveriesCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String status,
        String outDiv,
        String ordNo
) {
    public static GetFulfillmentDeliveriesCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv
    ) {
        return new GetFulfillmentDeliveriesCommand(customerCode, accessToken, startDate, endDate, status, outDiv, null);
    }

    public static GetFulfillmentDeliveriesCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String status,
            String outDiv,
            String ordNo
    ) {
        return new GetFulfillmentDeliveriesCommand(customerCode, accessToken, startDate, endDate, status, outDiv, ordNo);
    }
}
