package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;
import java.util.UUID;

public interface PublishOrderEventPort {

    void publishOrderPaymentCompletedEvent(Long orderId, Long buyerId, Long totalAmount,
                                           Long pointAmount, List<OrderProduct> orderProducts,
                                           Long totalAccumulatedPoint);

    void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<UUID> sharerKeys);

    void publishOrderCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                    Long cancelAmount, Long paymentAmount, Long pointAmount,
                                    Long shippingFee, boolean isFullCancel, Long alreadyRefunded,
                                    List<OrderProduct> orderProducts, List<OrderProduct> cancelProducts);
}
