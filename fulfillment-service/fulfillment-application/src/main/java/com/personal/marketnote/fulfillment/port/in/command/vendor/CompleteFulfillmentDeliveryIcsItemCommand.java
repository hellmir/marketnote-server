package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CompleteFulfillmentDeliveryIcsItemCommand(
        List<String> orderNumbers
) {
    public static CompleteFulfillmentDeliveryIcsItemCommand of(List<String> orderNumbers) {
        return new CompleteFulfillmentDeliveryIcsItemCommand(orderNumbers);
    }
}
