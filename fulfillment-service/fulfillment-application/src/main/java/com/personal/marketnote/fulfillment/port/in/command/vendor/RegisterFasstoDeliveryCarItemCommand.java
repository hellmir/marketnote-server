package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFasstoDeliveryCarItemCommand(
        String ordDt,
        String ordNo,
        String slipNo,
        String outWay,
        String cstShopCd,
        List<RegisterFasstoDeliveryGoodsCommand> godCds,
        String remark
) {
}
