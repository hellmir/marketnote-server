package com.personal.marketnote.commerce.port.out.fulfillment;

public interface SaveFulfillmentWorkStatusReadModelPort {
    void upsert(Long orderId, String workStatus);
}
