package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentDeliveryStatusItemResponse(
        String boxDiv,
        String boxDivNm,
        String boxNm,
        String boxNo,
        String boxTp,
        String crgSt,
        String crgStNm,
        String cstCd,
        String cstNm,
        String custAddr,
        String custNm,
        String custTelNo,
        String dlvMisYn,
        String godCd,
        String godNm,
        String invoiceNo,
        String ordNo,
        String ordSeq,
        String outDiv,
        String outDivNm,
        String outOrdSlipNo,
        String packDt,
        String packQty,
        String packSeq,
        String parcelCd,
        String parcelLinkYn,
        String parcelNm,
        String pickSeq,
        String postYn,
        String printCnt,
        String rtnAddr1,
        String rtnAddr2,
        String rtnCheck,
        String rtnEmpNm,
        String rtnOrdDt,
        String rtnTelNo,
        String rtnZipCd,
        String salChanel,
        String shipReqTerm,
        String shopCd,
        String shopNm,
        String sku,
        String whCd,
        List<Object> goodsSerialNo,
        String supCd,
        String supNm
) {
}
