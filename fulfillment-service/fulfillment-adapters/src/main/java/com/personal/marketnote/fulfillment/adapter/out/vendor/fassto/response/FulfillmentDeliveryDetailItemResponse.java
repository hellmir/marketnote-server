package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentDeliveryDetailItemResponse(
        String outDt,
        String ordDt,
        String whCd,
        String whNm,
        String slipNo,
        String cstCd,
        String cstNm,
        String cstShopCd,
        String shopCd,
        String mapSlipNo,
        String shopNm,
        Integer sku,
        Integer ordQty,
        Integer addGodOrdQty,
        String outDiv,
        String outDivNm,
        String ordNo,
        Integer ordSeq,
        String shipReqTerm,
        String salChanel,
        String outWay,
        String ordDiv,
        String outWayNm,
        String wrkStat,
        String wrkStatNm,
        String invoiceNo,
        String parcelNm,
        String parcelCd,
        String custNm,
        String custAddr,
        String custTelNo,
        String sendNm,
        String sendTelNo,
        String updUserNm,
        String updTime,
        List<FulfillmentDeliveryDetailGoodsResponse> goods,
        String remark
) {
}
