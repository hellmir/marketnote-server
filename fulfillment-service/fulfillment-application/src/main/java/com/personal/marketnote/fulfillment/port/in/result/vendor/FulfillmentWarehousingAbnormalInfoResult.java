package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentWarehousingAbnormalInfoResult(
        String slipNumber,
        String goodsSerialNo,
        String goodsSerialStatus,
        String warehouseCode,
        String customerCode,
        String customerName,
        String productCode,
        String description,
        String remark,
        String fileSequence,
        Integer lastFileSequenceNumber,
        String registeredDate,
        String registeredByName,
        String updatedDate,
        String updatedByName,
        String fileNumber,
        List<Object> imageUrl
) {
    public static FulfillmentWarehousingAbnormalInfoResult of(
            String slipNumber,
            String goodsSerialNo,
            String goodsSerialStatus,
            String warehouseCode,
            String customerCode,
            String customerName,
            String productCode,
            String description,
            String remark,
            String fileSequence,
            Integer lastFileSequenceNumber,
            String registeredDate,
            String registeredByName,
            String updatedDate,
            String updatedByName,
            String fileNumber,
            List<Object> imageUrl
    ) {
        return new FulfillmentWarehousingAbnormalInfoResult(
                slipNumber,
                goodsSerialNo,
                goodsSerialStatus,
                warehouseCode,
                customerCode,
                customerName,
                productCode,
                description,
                remark,
                fileSequence,
                lastFileSequenceNumber,
                registeredDate,
                registeredByName,
                updatedDate,
                updatedByName,
                fileNumber,
                imageUrl
        );
    }
}
