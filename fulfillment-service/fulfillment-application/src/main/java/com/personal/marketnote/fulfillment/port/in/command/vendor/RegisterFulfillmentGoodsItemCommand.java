package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record RegisterFulfillmentGoodsItemCommand(
        String productCode,
        String productName,
        String productType,
        String giftDivision,
        String productOptionCode1,
        String productOptionCode2,
        String inventoryProductNameUseYn,
        String inventoryProductName,
        String supplierCode,
        String categoryCode,
        String seasonCode,
        String genderCode,
        String manufactureYear,
        String productPrice,
        String inboundPrice,
        String salePrice,
        String dealTemperature,
        String pickFactor,
        String productBarcode,
        String boxWeight,
        String origin,
        String expirationManagementYn,
        String shelfLifeDays,
        String outboundCancelDays,
        String inboundCancelDays,
        String boxDivision,
        String bufferProductYn,
        String loadingDirection,
        String subMaterial,
        String useYn,
        String safetyStock,
        String feeYn,
        String saleUnitQuantity,
        String customerProductImageUrl,
        String externalProductImageUrl
) {
}
