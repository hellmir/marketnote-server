package com.personal.marketnote.user.port.in.result.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;

import java.util.List;
import java.util.stream.Collectors;

public record GetMyShippingAddressesResult(
        List<GetMyShippingAddressResult> shippingAddresses
) {
    public static GetMyShippingAddressesResult from(List<ShippingAddress> shippingAddresses) {
        return new GetMyShippingAddressesResult(
                shippingAddresses.stream()
                        .map(GetMyShippingAddressResult::from)
                        .collect(Collectors.toList())
        );
    }
}
