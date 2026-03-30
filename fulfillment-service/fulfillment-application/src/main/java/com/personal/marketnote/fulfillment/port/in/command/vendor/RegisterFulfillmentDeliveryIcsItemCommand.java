package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentDeliveryIcsItemCommand(
        String ordDt,
        String ordNo,
        String platform,
        String logiCenter,
        String invoiceNo,
        String custNm,
        String custTelNo,
        String custAddr,
        String sendNm,
        String sendTelNo,
        String salChanel,
        String shipReqTerm,
        String remark,
        List<RegisterFulfillmentDeliveryGoodsCommand> godCds
) {
}
