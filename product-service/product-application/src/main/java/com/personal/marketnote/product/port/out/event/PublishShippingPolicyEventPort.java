package com.personal.marketnote.product.port.out.event;

import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;

public interface PublishShippingPolicyEventPort {

    void publishShippingPolicyChangedEvent(Long sellerId, Long shippingFee, Long freeShippingThreshold,
                                           Long jejuSurcharge, Long islandSurcharge, ShippingPolicyChangeAction action);
}
