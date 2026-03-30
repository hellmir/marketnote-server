package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentWarehousingItemResult(
        String msg,
        String code,
        String slipNo,
        String ordNo
) {
    public static RegisterFulfillmentWarehousingItemResult of(
            String msg,
            String code,
            String slipNo,
            String ordNo
    ) {
        return new RegisterFulfillmentWarehousingItemResult(msg, code, slipNo, ordNo);
    }
}
