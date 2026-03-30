package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CancelFulfillmentDeliveryCommand(
        String customerCode,
        String accessToken,
        List<CancelFulfillmentDeliveryItemCommand> deliveries
) {
    public static CancelFulfillmentDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<CancelFulfillmentDeliveryItemCommand> deliveries
    ) {
        return new CancelFulfillmentDeliveryCommand(customerCode, accessToken, deliveries);
    }
}
