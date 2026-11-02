package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record RegisterFulfillmentDeliveryGoodsCommand(
        UUID productKey,
        String productCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static RegisterFulfillmentDeliveryGoodsCommand of(
            UUID productKey,
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productKey, productCode, expirationDate, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            UUID productKey,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productKey, null, null, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(null, productCode, null, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(null, productCode, expirationDate, orderQuantity);
    }
}
