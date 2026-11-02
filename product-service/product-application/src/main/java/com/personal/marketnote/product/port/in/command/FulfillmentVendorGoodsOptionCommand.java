package com.personal.marketnote.product.port.in.command;

import lombok.Builder;

@Builder
public record FulfillmentVendorGoodsOptionCommand(
        String goodsType,
        String giftDivision,
        String goodsOptionCode1,
        String goodsOptionCode2,
        String invoiceGoodsNameEnabled,
        String invoiceGoodsName,
        String supplierCode,
        String categoryCode,
        String seasonCode,
        String genderCode,
        String manufactureYear,
        String unitPrice,
        String supplyPrice,
        String salePrice,
        String handlingTemperature,
        String pickingFacility,
        String goodsBarcode,
        String boxWeight,
        String origin,
        String expirationDateManagementEnabled,
        String shelfLifeDays,
        String outboundAvailableDays,
        String inboundAvailableDays,
        String outboundBoxType,
        String cushioningEnabled,
        String loadingDirection,
        String subsidiaryMaterialCode,
        String enabled,
        String safetyStock,
        String feeApplied,
        String saleUnitQuantity,
        String customerGoodsImageUrl,
        String externalGoodsImageUrl
) {
}
