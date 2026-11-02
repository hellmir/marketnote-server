package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentOutOrdGoodsByOrdNoItemResponse(
        String ordNo,
        String invoiceNo,
        List<FulfillmentOutOrdGoodsByOrdNoGoodsResponse> goods
) {
}
