package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;

public interface SaveShippingAddressPort {
    ShippingAddress save(ShippingAddress shippingAddress);
}
