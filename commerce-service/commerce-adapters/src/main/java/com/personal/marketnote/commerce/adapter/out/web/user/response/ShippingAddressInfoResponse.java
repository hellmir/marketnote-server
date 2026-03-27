package com.personal.marketnote.commerce.adapter.out.web.user.response;

public record ShippingAddressInfoResponse(
        Long id,
        String addressType,
        String address,
        String addressDetail,
        String companyName,
        String addressAlias,
        String recipientName,
        String recipientPhoneNumber,
        String deliveryRequestType,
        String deliveryRequestMessage,
        boolean isDefault
) {
}
