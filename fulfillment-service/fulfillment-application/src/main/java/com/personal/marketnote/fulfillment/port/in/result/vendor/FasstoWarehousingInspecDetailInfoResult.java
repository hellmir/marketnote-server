package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FasstoWarehousingInspecDetailInfoResult(
        String ordDt,
        String whCd,
        String whNm,
        String slipNo,
        String cstCd,
        String cstNm,
        String supCd,
        String supNm,
        String inWay,
        String inWayNm,
        String godCd,
        Integer ordQty,
        Integer totInQty,
        String parcelComp,
        String parcelInvoiceNo,
        String wrkStat,
        String wrkStatNm,
        String remark,
        List<Object> goods,
        List<Object> goodsSerialNo,
        String externalGodImgUrl,
        String distTermDt
) {
    public static FasstoWarehousingInspecDetailInfoResult of(
            String ordDt,
            String whCd,
            String whNm,
            String slipNo,
            String cstCd,
            String cstNm,
            String supCd,
            String supNm,
            String inWay,
            String inWayNm,
            String godCd,
            Integer ordQty,
            Integer totInQty,
            String parcelComp,
            String parcelInvoiceNo,
            String wrkStat,
            String wrkStatNm,
            String remark,
            List<Object> goods,
            List<Object> goodsSerialNo,
            String externalGodImgUrl,
            String distTermDt
    ) {
        return new FasstoWarehousingInspecDetailInfoResult(
                ordDt,
                whCd,
                whNm,
                slipNo,
                cstCd,
                cstNm,
                supCd,
                supNm,
                inWay,
                inWayNm,
                godCd,
                ordQty,
                totInQty,
                parcelComp,
                parcelInvoiceNo,
                wrkStat,
                wrkStatNm,
                remark,
                goods,
                goodsSerialNo,
                externalGodImgUrl,
                distTermDt
        );
    }
}
