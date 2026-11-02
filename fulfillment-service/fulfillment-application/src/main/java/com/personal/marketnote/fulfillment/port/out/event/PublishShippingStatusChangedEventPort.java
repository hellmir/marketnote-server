package com.personal.marketnote.fulfillment.port.out.event;

import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;

public interface PublishShippingStatusChangedEventPort {
    void publish(ShippingStatusChangedEvent event);
}
