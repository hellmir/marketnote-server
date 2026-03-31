package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsCustomCodeNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsGiftDivisionNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsNameNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsTypeNoValueException;
import lombok.Builder;

@Builder
public record UpdateFulfillmentVendorGoodsCommand(
        String customerGoodsCode,
        String goodsName,
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
    public UpdateFulfillmentVendorGoodsCommand {
        if (FormatValidator.hasNoValue(customerGoodsCode)) {
            throw new FulfillmentVendorGoodsCustomCodeNoValueException();
        }
        if (FormatValidator.hasNoValue(goodsName)) {
            throw new FulfillmentVendorGoodsNameNoValueException();
        }
        if (FormatValidator.hasNoValue(goodsType)) {
            throw new FulfillmentVendorGoodsTypeNoValueException();
        }
        if (FormatValidator.hasNoValue(giftDivision)) {
            throw new FulfillmentVendorGoodsGiftDivisionNoValueException();
        }
    }
}
