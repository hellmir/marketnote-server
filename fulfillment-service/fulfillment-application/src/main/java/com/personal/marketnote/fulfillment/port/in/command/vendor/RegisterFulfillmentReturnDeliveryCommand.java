package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentReturnDeliveryCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentReturnDeliveryItemCommand> returnDeliveryRequests
) {
    public static RegisterFulfillmentReturnDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentReturnDeliveryItemCommand> returnDeliveryRequests
    ) {
        return new RegisterFulfillmentReturnDeliveryCommand(customerCode, accessToken, returnDeliveryRequests);
    }
}
