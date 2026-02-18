package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FasstoDeliveryGoodDetailItemResponse(
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
}
