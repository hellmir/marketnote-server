package com.personal.marketnote.product.adapter.in.event.saga.payload;

import java.util.List;

public record CartSagaPayload(
        Long orderId,
        Long buyerId,
        List<Long> pricePolicyIds
) {
}
