package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderCountResult;

public record GetOrderCountResponse(long totalCount) {
    public static GetOrderCountResponse from(GetOrderCountResult result) {
        return new GetOrderCountResponse(result.totalCount());
    }
}
