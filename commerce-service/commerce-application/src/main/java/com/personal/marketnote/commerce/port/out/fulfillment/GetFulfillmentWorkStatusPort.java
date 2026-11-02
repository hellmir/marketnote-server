package com.personal.marketnote.commerce.port.out.fulfillment;

public interface GetFulfillmentWorkStatusPort {
    String getWorkStatus(Long orderId);
}
