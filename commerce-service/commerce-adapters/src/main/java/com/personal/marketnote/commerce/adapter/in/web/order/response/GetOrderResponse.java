package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderResponse(
        GetOrderResult orderInfo
) {
    public static GetOrderResponse from(GetOrderResult getOrderResult) {
        return new GetOrderResponse(getOrderResult);
    }
}
