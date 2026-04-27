package com.personal.marketnote.commerce.port.out.shipping;

public interface SaveShippingPolicyReadModelPort {

    void upsert(Long sellerId, Long shippingFee, Long freeShippingThreshold);

    void deactivateBySellerId(Long sellerId);
}
