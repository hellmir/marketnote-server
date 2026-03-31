package com.personal.marketnote.product.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.ProductInfoNoValueException;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.out.fulfillment.RegisterFulfillmentVendorGoodsCommand;
import com.personal.marketnote.product.port.out.fulfillment.UpdateFulfillmentVendorGoodsCommand;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;

public class FulfillmentVendorGoodsCommandMapper {

    public static RegisterFulfillmentVendorGoodsCommand mapToRegisterCommand(
            Product product, FulfillmentVendorGoodsOptionCommand options
    ) {
        validateProduct(product);

        if (FormatValidator.hasNoValue(options)) {
            return RegisterFulfillmentVendorGoodsCommand.builder()
                    .customerGoodsCode(String.valueOf(product.getId()))
                    .goodsName(product.getName())
                    .goodsType(null)
                    .giftDivision(null)
                    .build();
        }

        return RegisterFulfillmentVendorGoodsCommand.builder()
                .customerGoodsCode(String.valueOf(product.getId()))
                .goodsName(product.getName())
                .goodsType(options.goodsType())
                .giftDivision(options.giftDivision())
                .goodsOptionCode1(options.goodsOptionCode1())
                .goodsOptionCode2(options.goodsOptionCode2())
                .invoiceGoodsNameEnabled(options.invoiceGoodsNameEnabled())
                .invoiceGoodsName(options.invoiceGoodsName())
                .supplierCode(options.supplierCode())
                .categoryCode(options.categoryCode())
                .seasonCode(options.seasonCode())
                .genderCode(options.genderCode())
                .manufactureYear(options.manufactureYear())
                .unitPrice(options.unitPrice())
                .supplyPrice(options.supplyPrice())
                .salePrice(options.salePrice())
                .handlingTemperature(options.handlingTemperature())
                .pickingFacility(options.pickingFacility())
                .goodsBarcode(options.goodsBarcode())
                .boxWeight(options.boxWeight())
                .origin(options.origin())
                .expirationDateManagementEnabled(options.expirationDateManagementEnabled())
                .shelfLifeDays(options.shelfLifeDays())
                .outboundAvailableDays(options.outboundAvailableDays())
                .inboundAvailableDays(options.inboundAvailableDays())
                .outboundBoxType(options.outboundBoxType())
                .cushioningEnabled(options.cushioningEnabled())
                .loadingDirection(options.loadingDirection())
                .subsidiaryMaterialCode(options.subsidiaryMaterialCode())
                .enabled(options.enabled())
                .safetyStock(options.safetyStock())
                .feeApplied(options.feeApplied())
                .saleUnitQuantity(options.saleUnitQuantity())
                .customerGoodsImageUrl(options.customerGoodsImageUrl())
                .externalGoodsImageUrl(options.externalGoodsImageUrl())
                .build();
    }

    public static UpdateFulfillmentVendorGoodsCommand mapToUpdateCommand(
            Product product, FulfillmentVendorGoodsOptionCommand options
    ) {
        validateProduct(product);

        if (FormatValidator.hasNoValue(options)) {
            throw new ProductInfoNoValueException("%s:: 풀필먼트 상품 수정 정보가 존재하지 않습니다.", FOURTH_ERROR_CODE);
        }

        return UpdateFulfillmentVendorGoodsCommand.builder()
                .customerGoodsCode(String.valueOf(product.getId()))
                .goodsName(product.getName())
                .goodsType(options.goodsType())
                .giftDivision(options.giftDivision())
                .goodsOptionCode1(options.goodsOptionCode1())
                .goodsOptionCode2(options.goodsOptionCode2())
                .invoiceGoodsNameEnabled(options.invoiceGoodsNameEnabled())
                .invoiceGoodsName(options.invoiceGoodsName())
                .supplierCode(options.supplierCode())
                .categoryCode(options.categoryCode())
                .seasonCode(options.seasonCode())
                .genderCode(options.genderCode())
                .manufactureYear(options.manufactureYear())
                .unitPrice(options.unitPrice())
                .supplyPrice(options.supplyPrice())
                .salePrice(options.salePrice())
                .handlingTemperature(options.handlingTemperature())
                .pickingFacility(options.pickingFacility())
                .goodsBarcode(options.goodsBarcode())
                .boxWeight(options.boxWeight())
                .origin(options.origin())
                .expirationDateManagementEnabled(options.expirationDateManagementEnabled())
                .shelfLifeDays(options.shelfLifeDays())
                .outboundAvailableDays(options.outboundAvailableDays())
                .inboundAvailableDays(options.inboundAvailableDays())
                .outboundBoxType(options.outboundBoxType())
                .cushioningEnabled(options.cushioningEnabled())
                .loadingDirection(options.loadingDirection())
                .subsidiaryMaterialCode(options.subsidiaryMaterialCode())
                .enabled(options.enabled())
                .safetyStock(options.safetyStock())
                .feeApplied(options.feeApplied())
                .saleUnitQuantity(options.saleUnitQuantity())
                .customerGoodsImageUrl(options.customerGoodsImageUrl())
                .externalGoodsImageUrl(options.externalGoodsImageUrl())
                .build();
    }

    private static void validateProduct(Product product) {
        if (FormatValidator.hasNoValue(product)) {
            throw new ProductInfoNoValueException("%s:: 상품이 존재하지 않습니다.", FIRST_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getId())) {
            throw new ProductInfoNoValueException("%s:: 상품 ID가 존재하지 않습니다.", SECOND_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getName())) {
            throw new ProductInfoNoValueException("%s:: 상품명이 존재하지 않습니다.", THIRD_ERROR_CODE);
        }
    }
}
