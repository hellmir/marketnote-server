package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentSettlementDailyCostItemResponse(
        String cloDt,
        String whCd,
        String cstCd,
        String inpAmt,
        String outAmt,
        String outcarAmt,
        String outairAmt,
        String keepAmt,
        String retAmt,
        String cashAmt,
        String founAmt,
        String othAmt,
        String totAmt
) {
}
