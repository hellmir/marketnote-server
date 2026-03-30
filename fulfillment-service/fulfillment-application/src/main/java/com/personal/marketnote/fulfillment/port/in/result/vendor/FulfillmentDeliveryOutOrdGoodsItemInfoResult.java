package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentDeliveryOutOrdGoodsItemInfoResult(
        String cstGodCd,
        String godNm,
        Integer packQty
) {
    public static FulfillmentDeliveryOutOrdGoodsItemInfoResult of(
            String cstGodCd,
            String godNm,
            Integer packQty
    ) {
        return new FulfillmentDeliveryOutOrdGoodsItemInfoResult(cstGodCd, godNm, packQty);
    }
}
