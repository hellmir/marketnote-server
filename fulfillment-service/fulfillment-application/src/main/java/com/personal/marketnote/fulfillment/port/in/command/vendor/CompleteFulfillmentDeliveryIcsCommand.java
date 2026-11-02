package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CompleteFulfillmentDeliveryIcsCommand(
        String customerCode,
        String accessToken,
        List<CompleteFulfillmentDeliveryIcsItemCommand> completionRequests
) {
    public static CompleteFulfillmentDeliveryIcsCommand of(
            String customerCode,
            String accessToken,
            List<CompleteFulfillmentDeliveryIcsItemCommand> completionRequests
    ) {
        return new CompleteFulfillmentDeliveryIcsCommand(customerCode, accessToken, completionRequests);
    }
}
