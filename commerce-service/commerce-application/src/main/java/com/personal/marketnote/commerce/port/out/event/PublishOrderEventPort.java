package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;

public interface PublishOrderEventPort {

    void publishOrderPaymentCompletedEvent(Long orderId, Long buyerId, Long totalAmount,
                                           Long pointAmount, List<OrderProduct> orderProducts,
                                           Long totalAccumulatedPoint);

    void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<Long> sharerIds);
}
