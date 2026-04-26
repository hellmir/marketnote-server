package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;

public interface ClassifyShippingAddressRegionPort {
    ShippingAddressRegionType classify(String address);
}
