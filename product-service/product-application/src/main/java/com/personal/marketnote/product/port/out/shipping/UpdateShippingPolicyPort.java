package com.personal.marketnote.product.port.out.shipping;

import com.personal.marketnote.product.domain.shipping.ShippingPolicy;

public interface UpdateShippingPolicyPort {

    void update(ShippingPolicy shippingPolicy);
}
