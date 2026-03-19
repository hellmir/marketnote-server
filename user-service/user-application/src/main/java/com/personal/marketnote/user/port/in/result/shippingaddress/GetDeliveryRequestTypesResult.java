package com.personal.marketnote.user.port.in.result.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;

public record GetDeliveryRequestTypesResult(
        DeliveryRequestType type,
        String description
) {
    public static GetDeliveryRequestTypesResult from(DeliveryRequestType deliveryRequestType) {
        return new GetDeliveryRequestTypesResult(
                deliveryRequestType,
                deliveryRequestType.getDescription()
        );
    }
}
