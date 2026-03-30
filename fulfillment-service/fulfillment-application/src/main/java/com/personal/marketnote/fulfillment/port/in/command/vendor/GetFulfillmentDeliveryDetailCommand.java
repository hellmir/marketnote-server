package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryDetailCommand(
        String customerCode,
        String accessToken,
        String slipNo,
        String ordNo
) {
    public static GetFulfillmentDeliveryDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNo
    ) {
        return new GetFulfillmentDeliveryDetailCommand(customerCode, accessToken, slipNo, null);
    }

    public static GetFulfillmentDeliveryDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return new GetFulfillmentDeliveryDetailCommand(customerCode, accessToken, slipNo, ordNo);
    }
}
