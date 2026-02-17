package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult(
        String cstGodCd,
        String godNm,
        Integer ordQty
) {
    public static FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult of(
            String cstGodCd,
            String godNm,
            Integer ordQty
    ) {
        return new FasstoDeliveryOutOrdGoodsByOrdNoItemInfoResult(cstGodCd, godNm, ordQty);
    }
}
