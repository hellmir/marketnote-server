package com.personal.marketnote.user.adapter.in.web.shippingaddress.response;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;

import java.util.List;
import java.util.stream.Collectors;

public record GetMyShippingAddressesResponse(
        List<GetMyShippingAddressResponse> shippingAddresses
) {
    public static GetMyShippingAddressesResponse from(GetMyShippingAddressesResult result) {
        return new GetMyShippingAddressesResponse(
                result.shippingAddresses().stream()
                        .map(GetMyShippingAddressResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
