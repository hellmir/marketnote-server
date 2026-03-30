package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentDeliveryItemResponse(
        String outDt,
        String ordDt,
        String whCd,
        String whNm,
        String slipNo,
        String cstCd,
        String cstNm,
        String shopCd,
        String mapSlipNo,
        String shopNm,
        Integer sku,
        Integer ordQty,
        Integer addGodOrdQty,
        String outDiv,
        String outDivNm,
        String cstShopCd,
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
        String updTime,
        String custNm,
        List<Object> goodsSerialNo,
        String custAddr,
        String supCd,
        String custTelNo,
        String supNm,
        String remark,
        String sendNm,
        String sendTelNo,
        String updUserNm
) {
}
