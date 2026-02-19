package com.personal.marketnote.user.port.in.result.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetMyShippingAddressResult(
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
    public static GetMyShippingAddressResult from(ShippingAddress shippingAddress) {
        return GetMyShippingAddressResult.builder()
                .id(shippingAddress.getId())
                .addressType(shippingAddress.getAddressType())
                .address(shippingAddress.getAddress())
                .addressDetail(shippingAddress.getAddressDetail())
                .companyName(shippingAddress.getCompanyName())
                .addressAlias(shippingAddress.getAddressAlias())
                .recipientName(shippingAddress.getRecipientName())
                .recipientPhoneNumber(shippingAddress.getRecipientPhoneNumber())
                .deliveryRequestType(shippingAddress.getDeliveryRequestType())
                .deliveryRequestMessage(shippingAddress.getDeliveryRequestMessage())
                .isDefault(shippingAddress.isDefault())
                .build();
    }
}
