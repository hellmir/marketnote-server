package com.personal.marketnote.fulfillment.port.out.event;

import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;

public interface PublishFulfillmentInventorySyncedEventPort {
    void publish(FulfillmentInventorySyncedEvent event);
}
