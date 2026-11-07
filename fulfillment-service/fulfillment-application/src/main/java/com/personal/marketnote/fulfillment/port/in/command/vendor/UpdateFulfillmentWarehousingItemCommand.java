package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

import lombok.Builder;

@Builder
public record UpdateFulfillmentWarehousingItemCommand(
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
        List<UpdateFulfillmentWarehousingGoodsCommand> products
) {
}
