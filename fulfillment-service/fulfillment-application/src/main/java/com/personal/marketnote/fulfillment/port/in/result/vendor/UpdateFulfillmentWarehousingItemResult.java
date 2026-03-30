package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentWarehousingItemResult(
        String msg,
        String code,
        String slipNo,
        String ordNo
) {
    public static UpdateFulfillmentWarehousingItemResult of(
            String msg,
            String code,
            String slipNo,
            String ordNo
    ) {
        return new UpdateFulfillmentWarehousingItemResult(msg, code, slipNo, ordNo);
    }
}
