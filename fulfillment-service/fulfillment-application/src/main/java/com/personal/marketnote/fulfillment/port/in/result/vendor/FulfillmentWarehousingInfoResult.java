package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentWarehousingInfoResult(
        String ordDt,
        String whCd,
        String whNm,
        String ordNo,
        String slipNo,
        String cstCd,
        String cstNm,
        String supCd,
        String cstSupCd,
        Integer sku,
        String supNm,
        Integer ordQty,
        Integer inQty,
        String inWay,
        String inWayNm,
        String parcelComp,
        String parcelInvoiceNo,
        String wrkStat,
        String wrkStatNm,
        String emgrYn,
        String remark,
        List<Object> goodsSerialNo
) {
    public static FulfillmentWarehousingInfoResult of(
            String ordDt,
            String whCd,
            String whNm,
            String ordNo,
            String slipNo,
            String cstCd,
            String cstNm,
            String supCd,
            String cstSupCd,
            Integer sku,
            String supNm,
            Integer ordQty,
            Integer inQty,
            String inWay,
            String inWayNm,
            String parcelComp,
            String parcelInvoiceNo,
            String wrkStat,
            String wrkStatNm,
            String emgrYn,
            String remark,
            List<Object> goodsSerialNo
    ) {
        return new FulfillmentWarehousingInfoResult(
                ordDt,
                whCd,
                whNm,
                ordNo,
                slipNo,
                cstCd,
                cstNm,
                supCd,
                cstSupCd,
                sku,
                supNm,
                ordQty,
                inQty,
                inWay,
                inWayNm,
                parcelComp,
                parcelInvoiceNo,
                wrkStat,
                wrkStatNm,
                emgrYn,
                remark,
                goodsSerialNo
        );
    }
}
