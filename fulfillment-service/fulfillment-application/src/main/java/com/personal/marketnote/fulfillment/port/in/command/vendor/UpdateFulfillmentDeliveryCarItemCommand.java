package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateFulfillmentDeliveryCarItemCommand(
        String orderDate,
        String orderNumber,
        String slipNumber,
        String releaseMethod,
        String shopCode,
        List<RegisterFulfillmentDeliveryGoodsCommand> products,
        String remark
) {
}
