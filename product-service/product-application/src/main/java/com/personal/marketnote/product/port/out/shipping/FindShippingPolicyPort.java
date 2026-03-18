package com.personal.marketnote.product.port.out.shipping;

import com.personal.marketnote.product.domain.shipping.ShippingPolicy;

import java.util.Optional;

public interface FindShippingPolicyPort {

    Optional<ShippingPolicy> findActiveBySellerId(Long sellerId);
}
