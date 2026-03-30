package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record RegisterFulfillmentDeliveryGoodsCommand(
        String cstGodCd,
        String distTermDt,
        Integer ordQty
) {
    public static RegisterFulfillmentDeliveryGoodsCommand of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        return new RegisterFulfillmentDeliveryGoodsCommand(cstGodCd, distTermDt, ordQty);
    }
}
