package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity.ShippingAddressJpaEntity;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressCreateState;

public class ShippingAddressJpaEntityToDomainMapper {

    public static ShippingAddress mapToDomain(ShippingAddressJpaEntity entity) {
        return ShippingAddress.from(
                ShippingAddressCreateState.builder()
                        .userId(entity.getUserId())
                        .addressType(entity.getAddressType())
                        .address(entity.getAddress())
                        .addressDetail(entity.getAddressDetail())
                        .companyName(entity.getCompanyName())
                        .addressAlias(entity.getAddressAlias())
                        .recipientName(entity.getRecipientName())
                        .recipientPhoneNumber(entity.getRecipientPhoneNumber())
                        .deliveryRequestType(entity.getDeliveryRequestType())
                        .deliveryRequestMessage(entity.getDeliveryRequestMessage())
                        .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                        .build(),
                entity.getId()
        );
    }
}
