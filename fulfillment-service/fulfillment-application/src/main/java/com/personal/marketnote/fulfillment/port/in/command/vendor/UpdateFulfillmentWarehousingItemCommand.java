package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record UpdateFulfillmentWarehousingItemCommand(
        String ordDt,
        String ordNo,
        String inWay,
        String slipNo,
        String parcelComp,
        String parcelInvoiceNo,
        String remark,
        String cstSupCd,
        String distTermDt,
        String makeDt,
        String preArv,
        List<UpdateFulfillmentWarehousingGoodsCommand> godCds
) {
    public static UpdateFulfillmentWarehousingItemCommand of(
            String ordDt,
            String ordNo,
            String inWay,
            String slipNo,
            String parcelComp,
            String parcelInvoiceNo,
            String remark,
            String cstSupCd,
            String distTermDt,
            String makeDt,
            String preArv,
            List<UpdateFulfillmentWarehousingGoodsCommand> godCds
    ) {
        return new UpdateFulfillmentWarehousingItemCommand(
                ordDt,
                ordNo,
                inWay,
                slipNo,
                parcelComp,
                parcelInvoiceNo,
                remark,
                cstSupCd,
                distTermDt,
                makeDt,
                preArv,
                godCds
        );
    }
}
