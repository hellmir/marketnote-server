package com.personal.marketnote.user.adapter.in.web.shippingaddress.response;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressResult;

public record GetMyShippingAddressResponse(
        Long id,
        ShippingAddressType addressType,
        String address,
        String addressDetail,
        String companyName,
        String addressAlias,
        String recipientName,
        String recipientPhoneNumber,
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage,
        boolean isDefault
) {
    public static GetMyShippingAddressResponse from(GetMyShippingAddressResult result) {
        return new GetMyShippingAddressResponse(
                result.id(),
                result.addressType(),
                result.address(),
                result.addressDetail(),
                result.companyName(),
                result.addressAlias(),
                result.recipientName(),
                result.recipientPhoneNumber(),
                result.deliveryRequestType(),
                result.deliveryRequestMessage(),
                result.isDefault()
        );
    }
}
