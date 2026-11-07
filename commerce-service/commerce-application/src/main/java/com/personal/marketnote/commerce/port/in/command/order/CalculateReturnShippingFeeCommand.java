package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import lombok.Builder;

import java.util.List;

@Builder
public record CalculateReturnShippingFeeCommand(
        Long orderId,
        OrderStatusReasonCategory reasonCategory,
        List<Long> returnPricePolicyIds
) {
    public static CalculateReturnShippingFeeCommand from(GetReturnRefundInfoCommand command) {
        return new CalculateReturnShippingFeeCommand(
                command.orderId(),
                command.reasonCategory(),
                command.returnPricePolicyIds()
        );
    }

    public static CalculateReturnShippingFeeCommand of(Order order, List<Long> returnPricePolicyIds) {
        return new CalculateReturnShippingFeeCommand(
                order.getId(),
                order.getStatusChangeReasonCategory(),
                returnPricePolicyIds
        );
    }
}
