package com.personal.marketnote.commerce.port.out.result.user;

public record ShippingAddressInfoResult(
        String recipientName,
        String recipientPhoneNumber,
        String address,
        String addressDetail,
        String regionType
) {
}
