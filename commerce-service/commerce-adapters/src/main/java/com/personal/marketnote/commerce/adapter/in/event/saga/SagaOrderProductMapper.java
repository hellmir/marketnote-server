package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.personal.marketnote.commerce.adapter.in.event.saga.payload.SagaOrderProductItem;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;

import java.util.List;

public final class SagaOrderProductMapper {

    private SagaOrderProductMapper() {
    }

    public static List<OrderProduct> toOrderProducts(List<SagaOrderProductItem> items) {
        return items.stream()
                .map(item -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .pricePolicyId(item.pricePolicyId())
                                .quantity(item.quantity())
                                .unitAmount(item.unitAmount())
                                .sharerId(item.sharerId())
                                .build()
                ))
                .toList();
    }
}
