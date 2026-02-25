package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderKeyResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderKeyResponse(
        String orderKey
) {
    public static GetOrderKeyResponse from(GetOrderKeyResult result) {
        return GetOrderKeyResponse.builder()
                .orderKey(result.orderKey())
                .build();
    }
}
