package com.personal.marketnote.product.mapper;

import com.personal.marketnote.product.domain.shipping.ShippingPolicyCreateState;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;

public class ShippingPolicyCommandToStateMapper {

    private ShippingPolicyCommandToStateMapper() {
    }

    public static ShippingPolicyCreateState mapToState(Long sellerId, RegisterShippingPolicyCommand command) {
        return ShippingPolicyCreateState.builder()
                .sellerId(sellerId)
                .deliveryCompany(command.deliveryCompany())
                .shippingFee(command.shippingFee())
                .freeShippingThreshold(command.freeShippingThreshold())
                .jejuSurcharge(command.jejuSurcharge())
                .islandSurcharge(command.islandSurcharge())
                .build();
    }
}
