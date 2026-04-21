package com.personal.marketnote.fulfillment.adapter.in.web.delivery.response;

import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;

public record GetFulfillmentWorkStatusResponse(
        Long orderId,
        String workStatus
) {
    public static GetFulfillmentWorkStatusResponse from(GetFulfillmentWorkStatusResult result) {
        return new GetFulfillmentWorkStatusResponse(
                result.orderId(),
                result.workStatus()
        );
    }
}
