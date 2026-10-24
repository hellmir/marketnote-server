package com.personal.marketnote.fulfillment.port.out.event;

public interface PublishOrderCancelFailedEventPort {
    void publish(Long orderId, Long buyerId);
}
