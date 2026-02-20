package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FasstoReturnGodDetailItemResponse(
        String ordNo,
        String supCd,
        String inSlipNo,
        String cstCd,
        String whCd,
        String inOrdSlipNo,
        String inOrdDt,
        String outOrdSlipNo,
        String supNm,
        String custNm,
        String outInvoiceNo,
        String rtnInvoiceNo,
        String inRtnPayCd,
        String inRtnPayNm,
        String inRtnPay,
        String rtnMisYn,
        String rtnType,
        String rtnTypeNm,
        String custTelNo,
        String cstMemo,
        String rtnReason,
        String rtnReasonNm,
        String rtnDetailReason,
        String rtnGubun,
        String rtnGubunNm,
        List<FasstoReturnGodDetailGoodsResponse> godList
) {
}
