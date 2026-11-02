package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;

public record ReturnInventorySagaPayload(
        Long orderId,
        List<SagaOrderProductItem> returnProducts,
        Boolean isFullReturn
) {
}
