package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record RegisterFulfillmentWarehousingGoodsCommand(
        String productCode,
        String expirationDate,
        Integer orderQuantity
) {
    public static RegisterFulfillmentWarehousingGoodsCommand of(
            String productCode,
            String expirationDate,
            Integer orderQuantity
    ) {
        return new RegisterFulfillmentWarehousingGoodsCommand(productCode, expirationDate, orderQuantity);
    }
}
