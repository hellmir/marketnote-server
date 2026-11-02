package com.personal.marketnote.commerce.port.in.command.saga;

import java.util.List;
import java.util.UUID;

public record OrderReturnSagaContext(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullReturn,
        Long returnShippingFee,
        Long refundPointAmount,
        List<ReturnProductItem> returnProducts,
        String returnId
) {
    public record ReturnProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
