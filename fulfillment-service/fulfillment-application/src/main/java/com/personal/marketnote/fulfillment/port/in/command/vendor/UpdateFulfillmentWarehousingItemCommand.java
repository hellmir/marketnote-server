package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

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
    public static UpdateFulfillmentWarehousingItemCommand of(
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
        return new UpdateFulfillmentWarehousingItemCommand(
                orderDate,
                orderNumber,
                warehousingMethod,
                slipNumber,
                courierCompany,
                trackingNumber,
                remark,
                supplierCode,
                expirationDate,
                manufacturingDate,
                preArrival,
                products
        );
    }
}
