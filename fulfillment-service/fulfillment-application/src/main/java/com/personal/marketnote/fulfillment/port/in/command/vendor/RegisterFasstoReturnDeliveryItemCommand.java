package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFasstoReturnDeliveryItemCommand(
        String ordDt,
        String ordNo,
        String parcelCd,
        String invoiceNo,
        String custNm,
        String custTelNo,
        String custAddr,
        String rtnEmpNm,
        String rtnTelNo,
        String rtnZipCd,
        String rtnAddr1,
        String rtnAddr2,
        String rtnGubun,
        String rtnReason,
        String getRtnDetailReason,
        String rtnShipReqTerm,
        List<RegisterFasstoDeliveryGoodsCommand> godCds
) {
}
