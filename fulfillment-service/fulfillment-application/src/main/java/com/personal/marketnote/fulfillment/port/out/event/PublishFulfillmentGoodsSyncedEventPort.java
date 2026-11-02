package com.personal.marketnote.fulfillment.port.out.event;

import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;

public interface PublishFulfillmentGoodsSyncedEventPort {
    void publish(FulfillmentGoodsSyncedEvent event);
}
