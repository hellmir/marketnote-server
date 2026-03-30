package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentSettlementDailyCostsResult(
        Integer dataCount,
        List<FulfillmentSettlementDailyCostInfoResult> dailyCosts
) {
    public static GetFulfillmentSettlementDailyCostsResult of(
            Integer dataCount,
            List<FulfillmentSettlementDailyCostInfoResult> dailyCosts
    ) {
        return new GetFulfillmentSettlementDailyCostsResult(dataCount, dailyCosts);
    }
}
