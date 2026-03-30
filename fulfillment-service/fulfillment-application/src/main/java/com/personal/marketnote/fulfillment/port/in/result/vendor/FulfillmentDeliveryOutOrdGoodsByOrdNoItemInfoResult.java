package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult(
        String cstGodCd,
        String godNm,
        Integer ordQty
) {
    public static FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult of(
            String cstGodCd,
            String godNm,
            Integer ordQty
    ) {
        return new FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult(cstGodCd, godNm, ordQty);
    }
}
