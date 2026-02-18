package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record CompleteFasstoDeliveryIcsItemResult(
        String code,
        String msg,
        String ordNo
) {
    public static CompleteFasstoDeliveryIcsItemResult of(
            String code,
            String msg,
            String ordNo
    ) {
        return new CompleteFasstoDeliveryIcsItemResult(code, msg, ordNo);
    }
}
