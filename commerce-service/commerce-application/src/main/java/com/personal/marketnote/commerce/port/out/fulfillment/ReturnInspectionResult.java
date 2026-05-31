package com.personal.marketnote.commerce.port.out.fulfillment;

import java.util.List;

public record ReturnInspectionResult(
        Integer dataCount,
        List<ReturnInspectionResultItem> returnGodInfos
) {
    public record ReturnInspectionResultItem(
            String orderNumber,
            String inboundOrderSlipNumber,
            List<ReturnInspectionGoodsItem> products
    ) {
    }

    public record ReturnInspectionGoodsItem(
            String customerProductCode,
            String productName,
            String returnProductCheckStatus,
            String returnProductCheckStatusName
    ) {
    }
}
