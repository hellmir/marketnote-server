package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentReturnGodDetailResult(
        Integer dataCount,
        List<FulfillmentReturnGodDetailInfoResult> returnGodInfos
) {
    public static GetFulfillmentReturnGodDetailResult of(
            Integer dataCount,
            List<FulfillmentReturnGodDetailInfoResult> returnGodInfos
    ) {
        return new GetFulfillmentReturnGodDetailResult(dataCount, returnGodInfos);
    }
}
