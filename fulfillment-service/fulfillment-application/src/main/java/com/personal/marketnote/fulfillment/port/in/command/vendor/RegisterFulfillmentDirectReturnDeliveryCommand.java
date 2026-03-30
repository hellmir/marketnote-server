package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record RegisterFulfillmentDirectReturnDeliveryCommand(
        String customerCode,
        String accessToken,
        List<RegisterFulfillmentDirectReturnDeliveryItemCommand> directReturnDeliveryRequests
) {
    public static RegisterFulfillmentDirectReturnDeliveryCommand of(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentDirectReturnDeliveryItemCommand> directReturnDeliveryRequests
    ) {
        return new RegisterFulfillmentDirectReturnDeliveryCommand(customerCode, accessToken, directReturnDeliveryRequests);
    }
}
