package com.personal.marketnote.user.port.in.result.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record RegisterShippingAddressResult(
        Long id,
        ShippingAddressType addressType,
        boolean isDefault,
        ShippingAddressRegionType regionType
) {
    public static RegisterShippingAddressResult from(ShippingAddress shippingAddress) {
        return RegisterShippingAddressResult.builder()
                .id(shippingAddress.getId())
                .addressType(shippingAddress.getAddressType())
                .isDefault(shippingAddress.isDefault())
                .regionType(shippingAddress.getRegionType())
                .build();
    }
}
