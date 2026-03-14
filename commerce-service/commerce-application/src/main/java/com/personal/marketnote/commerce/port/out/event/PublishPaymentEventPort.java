package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

import java.util.List;

public interface PublishPaymentEventPort {

    void publishPaymentApprovedEvent(Long orderId, String orderKey, Long paymentAmount);

    void publishPaymentFailedEvent(Long orderId, String orderKey, String resultCode, String resultMessage);

    void publishPaymentCancelledEvent(Long orderId, String orderKey, Long buyerId,
                                      Long cancelAmount, Long paymentAmount, Long pointAmount,
                                      boolean isFullCancel, Long alreadyRefunded,
                                      List<OrderProduct> orderProducts,
                                      List<OrderProduct> cancelProducts,
                                      Long partialProductPendingDeduction);
}
