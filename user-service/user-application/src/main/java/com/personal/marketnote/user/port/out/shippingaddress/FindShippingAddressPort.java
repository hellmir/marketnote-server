package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;

import java.util.List;

public interface FindShippingAddressPort {
    boolean existsByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    long countByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    boolean existsByUserId(Long userId);

    List<ShippingAddress> findAllByUserId(Long userId);
}
