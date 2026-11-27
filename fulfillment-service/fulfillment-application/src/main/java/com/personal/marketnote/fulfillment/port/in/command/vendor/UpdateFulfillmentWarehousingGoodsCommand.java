package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record UpdateFulfillmentWarehousingGoodsCommand(
    UUID productKey,
    String productCode,
    String fasstoProductCode,
    String expirationDate,
    Integer orderQuantity
) {
    public static UpdateFulfillmentWarehousingGoodsCommand of(
        String productCode,
        String expirationDate,
        Integer orderQuantity
    ) {
        return new UpdateFulfillmentWarehousingGoodsCommand(null, productCode, null, expirationDate, orderQuantity);
    }

    public static UpdateFulfillmentWarehousingGoodsCommand of(
        UUID productKey,
        String productCode,
        String expirationDate,
        Integer orderQuantity
    ) {
        return new UpdateFulfillmentWarehousingGoodsCommand(productKey, productCode, null, expirationDate, orderQuantity);
    }

    public static UpdateFulfillmentWarehousingGoodsCommand of(
        UUID productKey,
        String productCode,
        String fasstoProductCode,
        String expirationDate,
        Integer orderQuantity
    ) {
        return new UpdateFulfillmentWarehousingGoodsCommand(productKey, productCode, fasstoProductCode, expirationDate, orderQuantity);
    }
}
