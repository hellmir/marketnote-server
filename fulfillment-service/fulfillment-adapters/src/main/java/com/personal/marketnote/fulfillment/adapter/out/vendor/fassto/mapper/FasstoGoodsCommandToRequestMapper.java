package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoGoodsCommandToRequestMapper {
    public static FulfillmentGoodsMapper mapToRegisterRequest(RegisterFulfillmentGoodsCommand command) {
        List<FulfillmentGoodsItemMapper> goods = command.goods().stream()
                .map(FasstoGoodsCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentGoodsMapper.register(
                command.customerCode(),
                command.accessToken(),
                goods
        );
    }

    public static FulfillmentGoodsQuery mapToGoodsQuery(GetFulfillmentGoodsCommand command) {
        return FulfillmentGoodsQuery.of(
                command.customerCode(),
                command.accessToken()
        );
    }

    public static FulfillmentGoodsDetailQuery mapToGoodsDetailQuery(GetFulfillmentGoodsDetailCommand command) {
        return FulfillmentGoodsDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.productName()
        );
    }

    public static FulfillmentGoodsElementQuery mapToGoodsElementsQuery(GetFulfillmentGoodsElementsCommand command) {
        return FulfillmentGoodsElementQuery.of(
                command.customerCode(),
                command.accessToken()
        );
    }

    public static FulfillmentGoodsMapper mapToUpdateRequest(UpdateFulfillmentGoodsCommand command) {
        List<FulfillmentGoodsItemMapper> goods = command.goods().stream()
                .map(FasstoGoodsCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FulfillmentGoodsMapper.register(
                command.customerCode(),
                command.accessToken(),
                goods
        );
    }

    private static FulfillmentGoodsItemMapper mapItem(RegisterFulfillmentGoodsItemCommand item) {
        return FulfillmentGoodsItemMapper.of(
                item.productCode(),
                item.productName(),
                item.productType(),
                item.giftDivision(),
                item.productOptionCode1(),
                item.productOptionCode2(),
                item.inventoryProductNameUseYn(),
                item.inventoryProductName(),
                item.supplierCode(),
                item.categoryCode(),
                item.seasonCode(),
                item.genderCode(),
                item.manufactureYear(),
                item.productPrice(),
                item.inboundPrice(),
                item.salePrice(),
                item.dealTemperature(),
                item.pickFactor(),
                item.productBarcode(),
                item.boxWeight(),
                item.origin(),
                item.expirationManagementYn(),
                item.shelfLifeDays(),
                item.outboundCancelDays(),
                item.inboundCancelDays(),
                item.boxDivision(),
                item.bufferProductYn(),
                item.loadingDirection(),
                item.subMaterial(),
                item.useYn(),
                item.safetyStock(),
                item.feeYn(),
                item.saleUnitQuantity(),
                item.customerProductImageUrl(),
                item.externalProductImageUrl()
        );
    }

    private static FulfillmentGoodsItemMapper mapUpdateItem(UpdateFulfillmentGoodsItemCommand item) {
        return FulfillmentGoodsItemMapper.of(
                item.productCode(),
                item.productName(),
                item.productType(),
                item.giftDivision(),
                item.productOptionCode1(),
                item.productOptionCode2(),
                item.inventoryProductNameUseYn(),
                item.inventoryProductName(),
                item.supplierCode(),
                item.categoryCode(),
                item.seasonCode(),
                item.genderCode(),
                item.manufactureYear(),
                item.productPrice(),
                item.inboundPrice(),
                item.salePrice(),
                item.dealTemperature(),
                item.pickFactor(),
                item.productBarcode(),
                item.boxWeight(),
                item.origin(),
                item.expirationManagementYn(),
                item.shelfLifeDays(),
                item.outboundCancelDays(),
                item.inboundCancelDays(),
                item.boxDivision(),
                item.bufferProductYn(),
                item.loadingDirection(),
                item.subMaterial(),
                item.useYn(),
                item.safetyStock(),
                item.feeYn(),
                item.saleUnitQuantity(),
                item.customerProductImageUrl(),
                item.externalProductImageUrl()
        );
    }
}
