package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;

import java.util.List;

public record UpdateFulfillmentWarehousingResponse(
        Integer dataCount,
        List<UpdateFulfillmentWarehousingItemResult> warehousing
) {
    public static UpdateFulfillmentWarehousingResponse from(UpdateFulfillmentWarehousingResult result) {
        return new UpdateFulfillmentWarehousingResponse(result.dataCount(), result.warehousing());
    }
}
