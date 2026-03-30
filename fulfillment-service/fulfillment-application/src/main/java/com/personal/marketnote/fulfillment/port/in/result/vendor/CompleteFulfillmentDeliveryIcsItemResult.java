package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record CompleteFulfillmentDeliveryIcsItemResult(
        String code,
        String msg,
        String ordNo
) {
    public static CompleteFulfillmentDeliveryIcsItemResult of(
            String code,
            String msg,
            String ordNo
    ) {
        return new CompleteFulfillmentDeliveryIcsItemResult(code, msg, ordNo);
    }
}
