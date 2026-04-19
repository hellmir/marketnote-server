package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record CompleteFulfillmentDeliveryIcsItemResult(
        String code,
        String message,
        String orderNumber
) {
    public static CompleteFulfillmentDeliveryIcsItemResult of(
            String code,
            String message,
            String orderNumber
    ) {
        return new CompleteFulfillmentDeliveryIcsItemResult(code, message, orderNumber);
    }
}
