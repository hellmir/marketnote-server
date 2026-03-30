package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record UpdateFulfillmentWarehousingGoodsCommand(
        String cstGodCd,
        String distTermDt,
        Integer ordQty
) {
    public static UpdateFulfillmentWarehousingGoodsCommand of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        return new UpdateFulfillmentWarehousingGoodsCommand(cstGodCd, distTermDt, ordQty);
    }
}
