package com.personal.marketnote.commerce.port.out.user;

import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;

public interface FindUserShippingAddressPort {
    ShippingAddressInfoResult findByIdAndUserId(Long shippingAddressId, Long userId);
}
