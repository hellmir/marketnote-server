package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentWarehousingInspecDetailItemResponse(
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
}
