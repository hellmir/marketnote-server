package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFulfillmentDeliveryCarCommand(
        String customerCode,
        String accessToken,
        List<UpdateFulfillmentDeliveryCarItemCommand> deliveryRequests
) {
    public static UpdateFulfillmentDeliveryCarCommand of(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentDeliveryCarItemCommand> deliveryRequests
    ) {
        return new UpdateFulfillmentDeliveryCarCommand(customerCode, accessToken, deliveryRequests);
    }
}
