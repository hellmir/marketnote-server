package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentWarehousingDetailItemResponse(
        String ordDt,
        String whCd,
        String whNm,
        String slipNo,
        String ordNo,
        String cstCd,
        String cstNm,
        String supCd,
        String supNm,
        String cstSupCd,
        Integer sku,
        Integer ordQty,
        Integer inQty,
        Integer tarQty,
        String inWay,
        String inWayNm,
        String parcelComp,
        String parcelInvoiceNo,
        String wrkStat,
        String wrkStatNm,
        String emgrYn,
        String remark,
        List<FulfillmentWarehousingDetailGoodsResponse> goods
) {
}
