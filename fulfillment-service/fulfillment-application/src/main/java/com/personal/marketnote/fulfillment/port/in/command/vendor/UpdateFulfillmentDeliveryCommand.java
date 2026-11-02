package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFulfillmentDeliveryCommand(
        String customerCode,
        String accessToken,
        List<UpdateFulfillmentDeliveryItemCommand> deliveryRequests
) {
    public static UpdateFulfillmentDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentDeliveryItemCommand> deliveryRequests
    ) {
        return new UpdateFulfillmentDeliveryCommand(customerCode, accessToken, deliveryRequests);
    }
}
