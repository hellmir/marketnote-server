package com.personal.marketnote.commerce.port.out.event;

public interface PublishPaymentEventPort {

    void publishPaymentApprovedEvent(Long orderId, String orderKey, Long paymentAmount);

    void publishPaymentFailedEvent(Long orderId, String orderKey, String resultCode, String resultMessage);
}
