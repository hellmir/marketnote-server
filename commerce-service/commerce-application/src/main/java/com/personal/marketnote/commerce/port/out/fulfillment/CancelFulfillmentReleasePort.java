package com.personal.marketnote.commerce.port.out.fulfillment;

import java.util.List;

public interface CancelFulfillmentReleasePort {
    CancelFulfillmentReleaseResult cancelRelease(Long orderId, List<String> productKeys);
}
