package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;

public interface UpdateShippingAddressPort {
    void update(ShippingAddress shippingAddress);
}
