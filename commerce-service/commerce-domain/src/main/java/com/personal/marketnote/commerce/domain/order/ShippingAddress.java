package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.domain.delivery.PickupRequestType;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingAddress {
    private String recipientName;
    private String recipientPhoneNumber;
    private String zipCode;
    private String address;
    private String addressDetail;
    private DeliveryRequestType deliveryRequestType;
    private String deliveryRequestMessage;
    private PickupRequestType pickupRequestType;

    public static ShippingAddress of(
            String recipientName,
            String recipientPhoneNumber,
            String zipCode,
            String address,
            String addressDetail,
            DeliveryRequestType deliveryRequestType,
            String deliveryRequestMessage
    ) {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .deliveryRequestType(deliveryRequestType)
                .deliveryRequestMessage(deliveryRequestMessage)
                .build();
    }

    public static ShippingAddress ofPickup(
            String recipientName,
            String recipientPhoneNumber,
            String zipCode,
            String address,
            String addressDetail,
            PickupRequestType pickupRequestType,
            String pickupRequestMessage
    ) {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .pickupRequestType(pickupRequestType)
                .deliveryRequestMessage(pickupRequestMessage)
                .build();
    }

    public boolean hasRecipientName() {
        return FormatValidator.hasValue(recipientName);
    }

    public ShippingAddress withoutDeliveryRequest() {
        return ShippingAddress.builder()
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .zipCode(zipCode)
                .address(address)
                .addressDetail(addressDetail)
                .build();
    }
}
