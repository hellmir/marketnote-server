package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.goods.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentGoodsCommandToRequestMapper {
    public static FulfillmentGoodsMapper mapToRegisterRequest(RegisterFulfillmentGoodsCommand command) {
        List<FulfillmentGoodsItemMapper> goods = command.goods().stream()
                .map(FulfillmentGoodsCommandToRequestMapper::mapItem)
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
                command.godNm()
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
                .map(FulfillmentGoodsCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentGoodsMapper.register(
                command.customerCode(),
                command.accessToken(),
                goods
        );
    }

    private static FulfillmentGoodsItemMapper mapItem(RegisterFulfillmentGoodsItemCommand item) {
        return FulfillmentGoodsItemMapper.of(
                item.cstGodCd(),
                item.godNm(),
                item.godType(),
                item.giftDiv(),
                item.godOptCd1(),
                item.godOptCd2(),
                item.invGodNmUseYn(),
                item.invGodNm(),
                item.supCd(),
                item.cateCd(),
                item.seasonCd(),
                item.genderCd(),
                item.makeYr(),
                item.godPr(),
                item.inPr(),
                item.salPr(),
                item.dealTemp(),
                item.pickFac(),
                item.godBarcd(),
                item.boxWeight(),
                item.origin(),
                item.distTermMgtYn(),
                item.useTermDay(),
                item.outCanDay(),
                item.inCanDay(),
                item.boxDiv(),
                item.bufGodYn(),
                item.loadingDirection(),
                item.subMate(),
                item.useYn(),
                item.safetyStock(),
                item.feeYn(),
                item.saleUnitQty(),
                item.cstGodImgUrl(),
                item.externalGodImgUrl()
        );
    }

    private static FulfillmentGoodsItemMapper mapItem(UpdateFulfillmentGoodsItemCommand item) {
        return FulfillmentGoodsItemMapper.of(
                item.cstGodCd(),
                item.godNm(),
                item.godType(),
                item.giftDiv(),
                item.godOptCd1(),
                item.godOptCd2(),
                item.invGodNmUseYn(),
                item.invGodNm(),
                item.supCd(),
                item.cateCd(),
                item.seasonCd(),
                item.genderCd(),
                item.makeYr(),
                item.godPr(),
                item.inPr(),
                item.salPr(),
                item.dealTemp(),
                item.pickFac(),
                item.godBarcd(),
                item.boxWeight(),
                item.origin(),
                item.distTermMgtYn(),
                item.useTermDay(),
                item.outCanDay(),
                item.inCanDay(),
                item.boxDiv(),
                item.bufGodYn(),
                item.loadingDirection(),
                item.subMate(),
                item.useYn(),
                item.safetyStock(),
                item.feeYn(),
                item.saleUnitQty(),
                item.cstGodImgUrl(),
                item.externalGodImgUrl()
        );
    }
}
