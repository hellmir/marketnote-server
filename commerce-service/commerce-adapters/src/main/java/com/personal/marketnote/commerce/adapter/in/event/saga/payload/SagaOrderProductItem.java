package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.UUID;

public record SagaOrderProductItem(
        Long pricePolicyId,
        Long productId,
        UUID sharerKey,
        Integer quantity,
        Long unitAmount
) {
}
