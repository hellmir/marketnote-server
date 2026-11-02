package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;

public interface SaveFulfillmentGoodsReadModelPort {
    void upsert(FulfillmentGoodsSyncedEvent event);
}
