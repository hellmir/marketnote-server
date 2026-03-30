package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentStockInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;

import java.util.List;

public record GetFulfillmentStocksResponse(
        Integer dataCount,
        List<FulfillmentStockInfoResult> stocks
) {
    public static GetFulfillmentStocksResponse from(GetFulfillmentStocksResult result) {
        return new GetFulfillmentStocksResponse(result.dataCount(), result.stocks());
    }
}
