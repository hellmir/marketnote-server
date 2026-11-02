package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;

public record InventorySagaPayload(
        Long orderId,
        List<SagaOrderProductItem> orderProducts
) {
}
