package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.math.BigDecimal;

public record FasstoDeliveryGoodDetailInfoResult(
        String outDt,
        String slipNo,
        String outOrdSlipNo,
        String orderNo,
        String productOrderNo,
        String ordDiv,
        String invoiceNo,
        String sellerChannel,
        String custNm,
        String godCd,
        String cstGodCd,
        String godNm,
        Integer outQty,
        BigDecimal markedPrAmount,
        BigDecimal sellingPrAmount,
        BigDecimal dcAmount,
        BigDecimal sellerDcAmount,
        BigDecimal naverDcAmount
) {
    public static FasstoDeliveryGoodDetailInfoResult of(
            String outDt,
            String slipNo,
            String outOrdSlipNo,
            String orderNo,
            String productOrderNo,
            String ordDiv,
            String invoiceNo,
            String sellerChannel,
            String custNm,
            String godCd,
            String cstGodCd,
            String godNm,
            Integer outQty,
            BigDecimal markedPrAmount,
            BigDecimal sellingPrAmount,
            BigDecimal dcAmount,
            BigDecimal sellerDcAmount,
            BigDecimal naverDcAmount
    ) {
        return new FasstoDeliveryGoodDetailInfoResult(
                outDt, slipNo, outOrdSlipNo, orderNo, productOrderNo,
                ordDiv, invoiceNo, sellerChannel, custNm, godCd,
                cstGodCd, godNm, outQty, markedPrAmount, sellingPrAmount,
                dcAmount, sellerDcAmount, naverDcAmount
        );
    }
}
