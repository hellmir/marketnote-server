package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record RegisterFulfillmentDeliveryGoodsCommand(
        UUID productKey,
        String productCode,
        String fasstoProductCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static RegisterFulfillmentDeliveryGoodsCommand of(
            UUID productKey,
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productKey, productCode, null, expirationDate, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            UUID productKey,
            String productCode,
            String fasstoProductCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productKey, productCode, fasstoProductCode, expirationDate, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            UUID productKey,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productKey, null, null, null, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(null, productCode, null, null, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(null, productCode, null, expirationDate, orderQuantity);
    }
}
