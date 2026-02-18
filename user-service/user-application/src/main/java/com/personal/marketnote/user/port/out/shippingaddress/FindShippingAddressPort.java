package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;

public interface FindShippingAddressPort {
    boolean existsByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    long countByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    boolean existsByUserId(Long userId);
}
