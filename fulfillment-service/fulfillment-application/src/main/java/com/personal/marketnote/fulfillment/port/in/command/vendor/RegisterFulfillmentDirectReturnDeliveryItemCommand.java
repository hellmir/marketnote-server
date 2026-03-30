package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentDirectReturnDeliveryItemCommand(
        String ordDt,
        String supCd,
        String orgParcelCd,
        String orgInvoiceNo,
        String inWay,
        String custNm,
        String rtnParcelComp,
        String rtnInvoiceNo,
        String rtnGubun,
        String rtnReason,
        String rtnDetailReason,
        String remark,
        List<RegisterFulfillmentDeliveryGoodsCommand> godCds
) {
}
