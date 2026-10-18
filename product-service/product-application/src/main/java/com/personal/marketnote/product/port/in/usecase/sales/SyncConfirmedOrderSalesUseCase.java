package com.personal.marketnote.product.port.in.usecase.sales;

import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;

public interface SyncConfirmedOrderSalesUseCase {

    void syncSales(OrderPurchaseConfirmedEvent event);
}
