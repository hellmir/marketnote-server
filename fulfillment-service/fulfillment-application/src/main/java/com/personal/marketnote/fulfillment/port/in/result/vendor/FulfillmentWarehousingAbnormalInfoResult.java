package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
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
}
