package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentGoodsRequestToCommandMapper {
    public static RegisterFulfillmentGoodsCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFulfillmentGoodsRequest> request
    ) {
        List<RegisterFulfillmentGoodsItemCommand> goods = request.stream()
                .map(item -> RegisterFulfillmentGoodsItemCommand.builder()
                        .productCode(item.getCstGodCd())
                        .productName(item.getGodNm())
                        .productType(item.getGodType())
                        .giftDivision(item.getGiftDiv())
                        .productOptionCode1(item.getGodOptCd1())
                        .productOptionCode2(item.getGodOptCd2())
                        .inventoryProductNameUseYn(item.getInvGodNmUseYn())
                        .inventoryProductName(item.getInvGodNm())
                        .supplierCode(item.getSupCd())
                        .categoryCode(item.getCateCd())
                        .seasonCode(item.getSeasonCd())
                        .genderCode(item.getGenderCd())
                        .manufactureYear(item.getMakeYr())
                        .productPrice(item.getGodPr())
                        .inboundPrice(item.getInPr())
                        .salePrice(item.getSalPr())
                        .dealTemperature(item.getDealTemp())
                        .pickFactor(item.getPickFac())
                        .productBarcode(item.getGodBarcd())
                        .boxWeight(item.getBoxWeight())
                        .origin(item.getOrigin())
                        .expirationManagementYn(item.getDistTermMgtYn())
                        .shelfLifeDays(item.getUseTermDay())
                        .outboundCancelDays(item.getOutCanDay())
                        .inboundCancelDays(item.getInCanDay())
                        .boxDivision(item.getBoxDiv())
                        .bufferProductYn(item.getBufGodYn())
                        .loadingDirection(item.getLoadingDirection())
                        .subMaterial(item.getSubMate())
                        .useYn(item.getUseYn())
                        .safetyStock(item.getSafetyStock())
                        .feeYn(item.getFeeYn())
                        .saleUnitQuantity(item.getSaleUnitQty())
                        .customerProductImageUrl(item.getCstGodImgUrl())
                        .externalProductImageUrl(item.getExternalGodImgUrl())
                        .build())
                .toList();

        return RegisterFulfillmentGoodsCommand.of(customerCode, accessToken, goods);
    }

    public static GetFulfillmentGoodsCommand mapToGoodsCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFulfillmentGoodsCommand.of(customerCode, accessToken);
    }

    public static GetFulfillmentGoodsDetailCommand mapToGoodsDetailCommand(
            String customerCode,
            String accessToken,
            String godNm
    ) {
        return GetFulfillmentGoodsDetailCommand.of(customerCode, accessToken, godNm);
    }

    public static GetFulfillmentGoodsElementsCommand mapToGoodsElementsCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFulfillmentGoodsElementsCommand.of(customerCode, accessToken);
    }

    public static UpdateFulfillmentGoodsCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFulfillmentGoodsRequest> request
    ) {
        List<UpdateFulfillmentGoodsItemCommand> goods = request.stream()
                .map(item -> UpdateFulfillmentGoodsItemCommand.of(
                        item.getCstGodCd(),
                        item.getGodNm(),
                        item.getGodType(),
                        item.getGiftDiv(),
                        item.getGodOptCd1(),
                        item.getGodOptCd2(),
                        item.getInvGodNmUseYn(),
                        item.getInvGodNm(),
                        item.getSupCd(),
                        item.getCateCd(),
                        item.getSeasonCd(),
                        item.getGenderCd(),
                        item.getMakeYr(),
                        item.getGodPr(),
                        item.getInPr(),
                        item.getSalPr(),
                        item.getDealTemp(),
                        item.getPickFac(),
                        item.getGodBarcd(),
                        item.getBoxWeight(),
                        item.getOrigin(),
                        item.getDistTermMgtYn(),
                        item.getUseTermDay(),
                        item.getOutCanDay(),
                        item.getInCanDay(),
                        item.getBoxDiv(),
                        item.getBufGodYn(),
                        item.getLoadingDirection(),
                        item.getSubMate(),
                        item.getUseYn(),
                        item.getSafetyStock(),
                        item.getFeeYn(),
                        item.getSaleUnitQty(),
                        item.getCstGodImgUrl(),
                        item.getExternalGodImgUrl()
                ))
                .toList();

        return UpdateFulfillmentGoodsCommand.of(customerCode, accessToken, goods);
    }
}
