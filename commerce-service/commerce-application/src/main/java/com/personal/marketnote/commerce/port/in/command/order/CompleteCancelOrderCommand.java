package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CompleteCancelOrderCommand(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        String reasonCategory,
        String reason,
        List<CancelProductItemCommand> cancelProducts
) {
    public record CancelProductItemCommand(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
