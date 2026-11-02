package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record RegisterFulfillmentWarehousingGoodsCommand(
        UUID productKey,
        String productCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static RegisterFulfillmentWarehousingGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentWarehousingGoodsCommand(null, productCode, expirationDate, orderQuantity);
    }

    public static RegisterFulfillmentWarehousingGoodsCommand of(
            UUID productKey,
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentWarehousingGoodsCommand(productKey, productCode, expirationDate, orderQuantity);
    }
}
