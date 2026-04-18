package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record UpdateFulfillmentWarehousingGoodsCommand(
        String productCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static UpdateFulfillmentWarehousingGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new UpdateFulfillmentWarehousingGoodsCommand(productCode, expirationDate, orderQuantity);
    }
}
