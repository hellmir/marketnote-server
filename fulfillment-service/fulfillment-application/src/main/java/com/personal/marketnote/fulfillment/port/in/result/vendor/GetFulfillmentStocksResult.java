package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentStocksResult(
        Integer dataCount,
        List<FulfillmentStockInfoResult> stocks
) {
    public static GetFulfillmentStocksResult of(
            Integer dataCount,
            List<FulfillmentStockInfoResult> stocks
    ) {
        return new GetFulfillmentStocksResult(dataCount, stocks);
    }
}
