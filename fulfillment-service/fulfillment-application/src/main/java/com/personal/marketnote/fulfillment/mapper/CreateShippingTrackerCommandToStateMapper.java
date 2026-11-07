package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerCreateState;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;

public class CreateShippingTrackerCommandToStateMapper {
    private CreateShippingTrackerCommandToStateMapper() {
    }

    public static ShippingTrackerCreateState mapToCreateState(CreateShippingTrackerCommand command) {
        return ShippingTrackerCreateState.builder()
                .orderId(command.orderId())
                .buyerId(command.buyerId())
                .build();
    }
}
