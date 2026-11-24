package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationCreateState;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistrationCreateState;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;

public class FulfillmentCommandToStateMapper {
    private FulfillmentCommandToStateMapper() {
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
        RegisterFulfillmentGoodsItemCommand firstGoods = command.goods().getFirst();

        return FulfillmentGoodsRegistrationCreateState.builder()
                .productKey(firstGoods.productKey())
                .fasstoProductCode(firstGoods.fasstoProductCode())
                .build();
    }
}
