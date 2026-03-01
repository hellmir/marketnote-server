package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;

import java.util.List;

public record GetAdminOrdersResponse(
        List<GetOrderResponse> orders
) {
    public static GetAdminOrdersResponse from(GetAdminOrdersResult result) {
        List<GetOrderResponse> responses = result.orders().stream()
                .map(GetOrderResponse::from)
                .toList();
        return new GetAdminOrdersResponse(responses);
    }
}
