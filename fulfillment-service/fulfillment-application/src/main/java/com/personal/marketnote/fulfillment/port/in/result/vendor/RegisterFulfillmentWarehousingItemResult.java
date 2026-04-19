package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentWarehousingItemResult(
        String message,
        String code,
        String slipNumber,
        String orderNumber
) {
    public static RegisterFulfillmentWarehousingItemResult of(
            String message,
            String code,
            String slipNumber,
            String orderNumber
    ) {
        return new RegisterFulfillmentWarehousingItemResult(message, code, slipNumber, orderNumber);
    }
}
