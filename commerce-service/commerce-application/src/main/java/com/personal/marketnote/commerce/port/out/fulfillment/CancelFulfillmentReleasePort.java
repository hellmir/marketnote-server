package com.personal.marketnote.commerce.port.out.fulfillment;

public interface CancelFulfillmentReleasePort {
    CancelFulfillmentReleaseResult cancelRelease(Long orderId);
}
