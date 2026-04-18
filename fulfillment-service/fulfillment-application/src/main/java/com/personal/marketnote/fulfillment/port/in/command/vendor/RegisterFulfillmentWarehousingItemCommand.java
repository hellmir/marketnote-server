package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentWarehousingItemCommand(
        String orderDate,
        String orderNumber,
        String warehousingMethod,
        String slipNumber,
        String courierCompany,
        String trackingNumber,
        String remark,
        String supplierCode,
        String expirationDate,
        String manufacturingDate,
        String preArrival,
        List<RegisterFulfillmentWarehousingGoodsCommand> products
) {
}
