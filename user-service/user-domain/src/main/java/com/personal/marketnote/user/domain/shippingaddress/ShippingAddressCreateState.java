package com.personal.marketnote.user.domain.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShippingAddressCreateState {
    private final Long userId;
    private final ShippingAddressType addressType;
    private final String address;
    private final String addressDetail;
    private final String companyName;
    private final String addressAlias;
    private final String recipientName;
    private final String recipientPhoneNumber;
    private final DeliveryRequestType deliveryRequestType;
    private final String deliveryRequestMessage;
    private final boolean isDefault;
    private final ShippingAddressRegionType regionType;
}
