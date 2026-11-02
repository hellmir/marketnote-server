package com.personal.marketnote.commerce.port.out.shipping;

public interface SaveShippingPolicyReadModelPort {

    void upsert(Long sellerId, Long shippingFee, Long freeShippingThreshold,
                Long jejuSurcharge, Long islandSurcharge);

    void deactivateBySellerId(Long sellerId);
}
