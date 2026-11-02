package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderHistoryResult;

import java.util.List;

public record GetMyOrdersResponse(
        List<OrderHistoryByDateResponse> orderHistory
) {
    public static GetMyOrdersResponse from(GetBuyerOrderHistoryResult getBuyerOrderHistoryResult) {
        return new GetMyOrdersResponse(
                getBuyerOrderHistoryResult.orderHistory().stream()
                        .map(OrderHistoryByDateResponse::from)
                        .toList()
        );
    }
}
