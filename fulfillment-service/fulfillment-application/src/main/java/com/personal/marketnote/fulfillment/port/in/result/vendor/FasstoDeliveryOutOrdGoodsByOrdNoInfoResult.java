package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FasstoDeliveryOutOrdGoodsByOrdNoInfoResult(
        String ordNo,
        String invoiceNo,
        List<FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
) {
    public static FasstoDeliveryOutOrdGoodsByOrdNoInfoResult of(
            String ordNo,
            String invoiceNo,
            List<FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
    ) {
        return new FasstoDeliveryOutOrdGoodsByOrdNoInfoResult(ordNo, invoiceNo, goods);
    }
}
