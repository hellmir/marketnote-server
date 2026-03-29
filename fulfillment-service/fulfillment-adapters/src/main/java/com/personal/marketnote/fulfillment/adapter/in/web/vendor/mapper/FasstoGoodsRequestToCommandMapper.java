package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoGoodsRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoGoodsRequestToCommandMapper {
    public static RegisterFasstoGoodsCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoGoodsRequest> request
    ) {
        List<RegisterFasstoGoodsItemCommand> goods = request.stream()
                .map(item -> RegisterFasstoGoodsItemCommand.builder()
                        .cstGodCd(item.getCstGodCd())
                        .godNm(item.getGodNm())
                        .godType(item.getGodType())
                        .giftDiv(item.getGiftDiv())
                        .godOptCd1(item.getGodOptCd1())
                        .godOptCd2(item.getGodOptCd2())
                        .invGodNmUseYn(item.getInvGodNmUseYn())
                        .invGodNm(item.getInvGodNm())
                        .supCd(item.getSupCd())
                        .cateCd(item.getCateCd())
                        .seasonCd(item.getSeasonCd())
                        .genderCd(item.getGenderCd())
                        .makeYr(item.getMakeYr())
                        .godPr(item.getGodPr())
                        .inPr(item.getInPr())
                        .salPr(item.getSalPr())
                        .dealTemp(item.getDealTemp())
                        .pickFac(item.getPickFac())
                        .godBarcd(item.getGodBarcd())
                        .boxWeight(item.getBoxWeight())
                        .origin(item.getOrigin())
                        .distTermMgtYn(item.getDistTermMgtYn())
                        .useTermDay(item.getUseTermDay())
                        .outCanDay(item.getOutCanDay())
                        .inCanDay(item.getInCanDay())
                        .boxDiv(item.getBoxDiv())
                        .bufGodYn(item.getBufGodYn())
                        .loadingDirection(item.getLoadingDirection())
                        .subMate(item.getSubMate())
                        .useYn(item.getUseYn())
                        .safetyStock(item.getSafetyStock())
                        .feeYn(item.getFeeYn())
                        .saleUnitQty(item.getSaleUnitQty())
                        .cstGodImgUrl(item.getCstGodImgUrl())
                        .externalGodImgUrl(item.getExternalGodImgUrl())
                        .build())
                .toList();

        return RegisterFasstoGoodsCommand.of(customerCode, accessToken, goods);
    }

    public static GetFasstoGoodsCommand mapToGoodsCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFasstoGoodsCommand.of(customerCode, accessToken);
    }

    public static GetFasstoGoodsDetailCommand mapToGoodsDetailCommand(
            String customerCode,
            String accessToken,
            String godNm
    ) {
        return GetFasstoGoodsDetailCommand.of(customerCode, accessToken, godNm);
    }

    public static GetFasstoGoodsElementsCommand mapToGoodsElementsCommand(
            String customerCode,
            String accessToken
    ) {
        return GetFasstoGoodsElementsCommand.of(customerCode, accessToken);
    }

    public static UpdateFasstoGoodsCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFasstoGoodsRequest> request
    ) {
        List<UpdateFasstoGoodsItemCommand> goods = request.stream()
                .map(item -> UpdateFasstoGoodsItemCommand.of(
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

        return UpdateFasstoGoodsCommand.of(customerCode, accessToken, goods);
    }
}
