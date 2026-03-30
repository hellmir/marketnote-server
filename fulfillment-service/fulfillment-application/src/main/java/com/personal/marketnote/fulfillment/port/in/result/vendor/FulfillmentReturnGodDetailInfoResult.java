package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentReturnGodDetailInfoResult(
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
        List<FulfillmentReturnGodDetailGoodsResult> godList
) {
    public static FulfillmentReturnGodDetailInfoResult of(
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
            List<FulfillmentReturnGodDetailGoodsResult> godList
    ) {
        return new FulfillmentReturnGodDetailInfoResult(
                ordNo, supCd, inSlipNo, cstCd, whCd, inOrdSlipNo, inOrdDt,
                outOrdSlipNo, supNm, custNm, outInvoiceNo, rtnInvoiceNo,
                inRtnPayCd, inRtnPayNm, inRtnPay, rtnMisYn, rtnType, rtnTypeNm,
                custTelNo, cstMemo, rtnReason, rtnReasonNm, rtnDetailReason,
                rtnGubun, rtnGubunNm, godList
        );
    }
}
