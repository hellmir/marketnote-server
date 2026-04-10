package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import lombok.Builder;

@Builder
public record CancelOrderCommand(
        Long id,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        Long buyerId
) {
}
