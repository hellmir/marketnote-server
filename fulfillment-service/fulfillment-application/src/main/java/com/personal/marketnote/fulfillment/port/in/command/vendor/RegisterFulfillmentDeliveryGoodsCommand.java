package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record RegisterFulfillmentDeliveryGoodsCommand(
        String productCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productCode, expirationDate, orderQuantity);
    }

    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String productCode,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(productCode, null, orderQuantity);
    }
}
