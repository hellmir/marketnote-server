package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentDeliveryIcsCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentDeliveryIcsItemCommand> deliveryRequests
) {
    public static RegisterFulfillmentDeliveryIcsCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDeliveryIcsItemCommand> deliveryRequests
    ) {
        return new RegisterFulfillmentDeliveryIcsCommand(customerCode, accessToken, deliveryRequests);
    }
}
