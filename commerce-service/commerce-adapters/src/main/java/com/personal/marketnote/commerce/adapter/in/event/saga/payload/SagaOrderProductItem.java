package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record SagaOrderProductItem(
        Long pricePolicyId,
        Long sharerId,
        Integer quantity,
        Long unitAmount
) {
}
