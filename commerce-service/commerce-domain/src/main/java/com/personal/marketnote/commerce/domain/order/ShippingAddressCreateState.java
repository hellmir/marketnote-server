package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingAddressCreateState {
    private final String recipientName;
    private final String recipientPhoneNumber;
    private final String zipCode;
    private final String address;
    private final String addressDetail;
    private final DeliveryRequestType deliveryRequestType;
    private final String deliveryRequestMessage;

    public static ShippingAddressCreateState forShipping(
            String recipientName,
            String recipientPhoneNumber,
            String address,
            String addressDetail,
            DeliveryRequestType deliveryRequestType,
            String deliveryRequestMessage
    ) {
        return ShippingAddressCreateState.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .address(address)
                .addressDetail(addressDetail)
                .deliveryRequestType(deliveryRequestType)
                .deliveryRequestMessage(deliveryRequestMessage)
                .build();
    }

    public static ShippingAddressCreateState forReturnPickup(
            String recipientName,
            String recipientPhoneNumber,
            String zipCode,
            String address,
            String addressDetail,
            String pickupRequestMessage
    ) {
        return ShippingAddressCreateState.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .deliveryRequestMessage(pickupRequestMessage)
                .build();
    }
}
