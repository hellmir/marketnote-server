package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record RegisterFulfillmentWarehousingGoodsCommand(
        String cstGodCd,
        String distTermDt,
        Integer ordQty
) {
    public static RegisterFulfillmentWarehousingGoodsCommand of(
            String cstGodCd,
            String distTermDt,
            Integer ordQty
    ) {
        return new RegisterFulfillmentWarehousingGoodsCommand(cstGodCd, distTermDt, ordQty);
    }
}
