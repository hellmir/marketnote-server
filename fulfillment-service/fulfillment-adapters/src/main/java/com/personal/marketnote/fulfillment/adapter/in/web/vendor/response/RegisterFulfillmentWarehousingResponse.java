package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;

import java.util.List;

public record RegisterFulfillmentWarehousingResponse(
        Integer dataCount,
        List<RegisterFulfillmentWarehousingItemResult> warehousing
) {
    public static RegisterFulfillmentWarehousingResponse from(RegisterFulfillmentWarehousingResult result) {
        return new RegisterFulfillmentWarehousingResponse(result.dataCount(), result.warehousing());
    }
}
