package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CompleteFulfillmentDeliveryIcsItemCommand(
        List<String> ordNoList
) {
    public static CompleteFulfillmentDeliveryIcsItemCommand of(List<String> ordNoList) {
        return new CompleteFulfillmentDeliveryIcsItemCommand(ordNoList);
    }
}
