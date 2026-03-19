package com.personal.marketnote.user.adapter.in.web.shippingaddress.response;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;

public record GetDeliveryRequestTypesResponse(
        String type,
        String description
) {
    public static GetDeliveryRequestTypesResponse from(GetDeliveryRequestTypesResult result) {
        return new GetDeliveryRequestTypesResponse(
                result.type().name(),
                result.description()
        );
    }
}
