package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderProductKeyResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderProductKeyResponse(
        String orderProductKey
) {
    public static GetOrderProductKeyResponse from(GetOrderProductKeyResult result) {
        return GetOrderProductKeyResponse.builder()
                .orderProductKey(result.orderProductKey())
                .build();
    }
}
