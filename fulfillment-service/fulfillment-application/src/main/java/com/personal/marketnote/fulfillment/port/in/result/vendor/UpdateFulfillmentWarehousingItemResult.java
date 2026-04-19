package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentWarehousingItemResult(
        String message,
        String code,
        String slipNumber,
        String orderNumber
) {
    public static UpdateFulfillmentWarehousingItemResult of(
            String message,
            String code,
            String slipNumber,
            String orderNumber
    ) {
        return new UpdateFulfillmentWarehousingItemResult(message, code, slipNumber, orderNumber);
    }
}
