package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import lombok.Builder;

@Builder
public record RejectReturnCommand(
        Long id,
        OrderStatusReasonCategory reasonCategory,
        String reason
) {
}
