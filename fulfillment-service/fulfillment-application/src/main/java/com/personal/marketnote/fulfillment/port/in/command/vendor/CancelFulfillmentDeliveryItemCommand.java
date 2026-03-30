package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record CancelFulfillmentDeliveryItemCommand(
        String slipNo,
        String ordNo
) {
    public static CancelFulfillmentDeliveryItemCommand of(
            String slipNo,
            String ordNo
    ) {
        return new CancelFulfillmentDeliveryItemCommand(slipNo, ordNo);
    }
}
