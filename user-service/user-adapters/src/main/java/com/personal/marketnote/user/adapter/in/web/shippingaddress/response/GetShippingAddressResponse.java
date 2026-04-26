package com.personal.marketnote.user.adapter.in.web.shippingaddress.response;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;

public record GetShippingAddressResponse(
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
        boolean isDefault,
        ShippingAddressRegionType regionType
) {
    public static GetShippingAddressResponse from(GetShippingAddressResult result) {
        return new GetShippingAddressResponse(
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
                result.isDefault(),
                result.regionType()
        );
    }
}
