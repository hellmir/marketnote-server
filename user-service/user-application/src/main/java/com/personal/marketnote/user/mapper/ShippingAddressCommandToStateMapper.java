package com.personal.marketnote.user.mapper;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;

public class ShippingAddressCommandToStateMapper {
    private ShippingAddressCommandToStateMapper() {
    }

    public static ShippingAddressCreateState mapToCreateState(
            RegisterShippingAddressCommand command, boolean isDefault, ShippingAddressRegionType regionType
    ) {
        return ShippingAddressCreateState.builder()
                .userId(command.userId())
                .addressType(command.addressType())
                .address(command.address())
                .addressDetail(command.addressDetail())
                .companyName(command.companyName())
                .addressAlias(command.addressAlias())
                .recipientName(command.recipientName())
                .recipientPhoneNumber(command.recipientPhoneNumber())
                .deliveryRequestType(
                        FormatValidator.hasValue(command.deliveryRequestType())
                                ? command.deliveryRequestType()
                                : DeliveryRequestType.NONE
                )
                .deliveryRequestMessage(command.deliveryRequestMessage())
                .isDefault(isDefault)
                .regionType(regionType)
                .build();
    }
}
