package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;
import java.util.UUID;

public interface PublishOrderEventPort {

    void publishOrderPaymentCompletedEvent(Long orderId, Long buyerId, Long totalAmount,
                                           Long pointAmount, List<OrderProduct> orderProducts,
                                           Long totalAccumulatedPoint);

    void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<UUID> sharerKeys);
}
