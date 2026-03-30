package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentStockInfoResult(
        String whCd,
        String godCd,
        String cstGodCd,
        String godNm,
        String distTermDt,
        String distTermMgtYn,
        String godBarcd,
        Integer stockQty,
        Integer badStockQty,
        Integer canStockQty,
        String cstSupCd,
        String supNm,
        String giftDiv,
        List<Object> goodsSerialNo,
        String slipNo
) {
    public static FulfillmentStockInfoResult of(
            String whCd,
            String godCd,
            String cstGodCd,
            String godNm,
            String distTermDt,
            String distTermMgtYn,
            String godBarcd,
            Integer stockQty,
            Integer badStockQty,
            Integer canStockQty,
            String cstSupCd,
            String supNm,
            String giftDiv,
            List<Object> goodsSerialNo,
            String slipNo
    ) {
        return new FulfillmentStockInfoResult(
                whCd,
                godCd,
                cstGodCd,
                godNm,
                distTermDt,
                distTermMgtYn,
                godBarcd,
                stockQty,
                badStockQty,
                canStockQty,
                cstSupCd,
                supNm,
                giftDiv,
                goodsSerialNo,
                slipNo
        );
    }
}
