package com.personal.marketnote.product.port.out.sales;

public interface OrderSalesSyncLogPort {

    boolean isProcessed(Long orderId);

    void markProcessed(Long orderId);
}
