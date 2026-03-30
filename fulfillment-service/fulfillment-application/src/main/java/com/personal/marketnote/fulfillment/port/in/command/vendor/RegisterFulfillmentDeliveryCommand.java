package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentDeliveryCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentDeliveryItemCommand> deliveryRequests
) {
    public static RegisterFulfillmentDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryItemCommand> deliveryRequests
    ) {
        return new RegisterFulfillmentDeliveryCommand(customerCode, accessToken, deliveryRequests);
    }
}
