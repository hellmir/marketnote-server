package com.personal.marketnote.fulfillment.port.out.event;

import com.personal.marketnote.common.kafka.event.FulfillmentDeliveryWorkStatusChangedEvent;

public interface PublishFulfillmentDeliveryWorkStatusChangedEventPort {
    void publish(FulfillmentDeliveryWorkStatusChangedEvent event);
}
