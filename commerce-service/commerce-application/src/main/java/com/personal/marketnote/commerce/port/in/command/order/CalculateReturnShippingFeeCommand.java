package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import lombok.Builder;

import java.util.List;

@Builder
public record CalculateReturnShippingFeeCommand(
        Long orderId,
        OrderStatusReasonCategory reasonCategory,
        List<Long> returnPricePolicyIds
) {
}
