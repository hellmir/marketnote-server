package com.personal.marketnote.product.mapper;

import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.ProductInfoNoValueException;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;

public class ProductUpdatedEventMapper {

    public static ProductUpdatedEvent mapToEvent(Product product, FulfillmentVendorGoodsOptionCommand options) {
        if (FormatValidator.hasNoValue(product)) {
            throw new ProductInfoNoValueException("%s:: 상품이 존재하지 않습니다.", FIRST_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getId())) {
            throw new ProductInfoNoValueException("%s:: 상품 ID가 존재하지 않습니다.", SECOND_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getName())) {
            throw new ProductInfoNoValueException("%s:: 상품명이 존재하지 않습니다.", THIRD_ERROR_CODE);
        }

        return new ProductUpdatedEvent(
                product.getId(),
                product.getName(),
                options.goodsType(),
                options.giftDivision(),
                options.goodsOptionCode1(),
                options.goodsOptionCode2(),
                options.invoiceGoodsNameEnabled(),
                options.invoiceGoodsName(),
                options.supplierCode(),
                options.categoryCode(),
                options.seasonCode(),
                options.genderCode(),
                options.manufactureYear(),
                options.unitPrice(),
                options.supplyPrice(),
                options.salePrice(),
                options.handlingTemperature(),
                options.pickingFacility(),
                options.goodsBarcode(),
                options.boxWeight(),
                options.origin(),
                options.expirationDateManagementEnabled(),
                options.shelfLifeDays(),
                options.outboundAvailableDays(),
                options.inboundAvailableDays(),
                options.outboundBoxType(),
                options.cushioningEnabled(),
                options.loadingDirection(),
                options.subsidiaryMaterialCode(),
                options.enabled(),
                options.safetyStock(),
                options.feeApplied(),
                options.saleUnitQuantity(),
                options.customerGoodsImageUrl(),
                options.externalGoodsImageUrl()
        );
    }
}
