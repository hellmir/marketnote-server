package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;

import java.util.List;
import java.util.UUID;

public interface PublishOrderEventPort {

    void publishOrderPaymentCompletedEvent(
            Long orderId, Long buyerId, Long totalAmount,
            Long pointAmount, ShippingAddress shippingAddress,
            List<OrderProduct> orderProducts, Long totalAccumulatedPoint
    );

    void publishOrderPurchaseConfirmedEvent(
            Long orderId, Long buyerId, List<UUID> sharerKeys, boolean isAutoConfirmed,
            List<OrderProduct> orderProducts
    );

    void publishOrderCancelledEvent(
            Long orderId, String orderKey, Long buyerId,
            Long cancelAmount, Long paymentAmount, Long pointAmount,
            Long shippingFee, boolean isFullCancel, Long alreadyRefunded,
            List<OrderProduct> orderProducts, List<OrderProduct> cancelProducts,
            String cancelId
    );

    void publishOrderReturnedEvent(Long orderId, String orderKey, Long buyerId,
                                   Long returnAmount, Long paymentAmount, Long pointAmount,
                                   Long shippingFee, boolean isFullReturn,
                                   Long returnShippingFee, List<OrderProduct> returnProducts,
                                   String returnId);

    void publishOrderCancelFailedEvent(Long orderId, Long buyerId);

    void publishReturnRequestedEvent(Long orderId, Long buyerId);

    void publishReturnRejectedEvent(Long orderId, Long buyerId);
}
