package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentWarehousingItemResponse(
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
}
