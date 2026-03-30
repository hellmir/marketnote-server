package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateFulfillmentDeliveryItemCommand(
        String ordDt,
        String ordNo,
        Integer ordSeq,
        String slipNo,
        String custNm,
        String custTelNo,
        String custAddr,
        String outWay,
        String sendNm,
        String sendTelNo,
        String salChanel,
        String shipReqTerm,
        List<RegisterFulfillmentDeliveryGoodsCommand> godCds,
        String oneDayDeliveryCd,
        String remark
) {
}
