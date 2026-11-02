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
}
