package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationCreateState;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistrationCreateState;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;

public class FasstoCommandToStateMapper {
    private FasstoCommandToStateMapper() {
    }

    public static FulfillmentDeliveryRegistrationCreateState mapToDeliveryRegistrationCreateState(
            RegisterFulfillmentDeliveryCommand command
    ) {
        Long orderId = Long.parseLong(command.deliveryRequests().getFirst().orderNumber());

        return FulfillmentDeliveryRegistrationCreateState.builder()
                .orderId(orderId)
                .build();
    }

    public static FulfillmentGoodsRegistrationCreateState mapToGoodsRegistrationCreateState(
            RegisterFulfillmentGoodsCommand command
    ) {
        Long productId = Long.parseLong(command.goods().getFirst().cstGodCd());

        return FulfillmentGoodsRegistrationCreateState.builder()
                .productId(productId)
                .build();
    }
}
