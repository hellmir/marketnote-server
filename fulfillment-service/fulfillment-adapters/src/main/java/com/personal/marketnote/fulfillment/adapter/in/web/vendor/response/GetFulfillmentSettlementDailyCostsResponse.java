package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentSettlementDailyCostInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;

import java.util.List;

public record GetFulfillmentSettlementDailyCostsResponse(
        Integer dataCount,
        List<FulfillmentSettlementDailyCostInfoResult> dailyCosts
) {
    public static GetFulfillmentSettlementDailyCostsResponse from(GetFulfillmentSettlementDailyCostsResult result) {
        return new GetFulfillmentSettlementDailyCostsResponse(result.dataCount(), result.dailyCosts());
    }
}
