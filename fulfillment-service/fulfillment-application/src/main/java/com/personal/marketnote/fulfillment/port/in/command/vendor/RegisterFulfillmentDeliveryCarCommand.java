package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentDeliveryCarCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentDeliveryCarItemCommand> deliveryRequests
) {
    public static RegisterFulfillmentDeliveryCarCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryCarItemCommand> deliveryRequests
    ) {
        return new RegisterFulfillmentDeliveryCarCommand(customerCode, accessToken, deliveryRequests);
    }
}
