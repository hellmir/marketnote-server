package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.warehousing.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FulfillmentWarehousingCommandToRequestMapper {
    public static FulfillmentWarehousingMapper mapToRegisterRequest(RegisterFulfillmentWarehousingCommand command) {
        List<FulfillmentWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FulfillmentWarehousingCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentWarehousingMapper.register(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    public static FulfillmentWarehousingQuery mapToQuery(GetFulfillmentWarehousingCommand command) {
        return FulfillmentWarehousingQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.inWay(),
                command.ordNo(),
                command.wrkStat()
        );
    }

    public static FulfillmentWarehousingDetailQuery mapToDetailQuery(GetFulfillmentWarehousingDetailCommand command) {
        return FulfillmentWarehousingDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNo(),
                command.ordNo()
        );
    }

    public static FulfillmentWarehousingAbnormalQuery mapToAbnormalQuery(GetFulfillmentWarehousingAbnormalCommand command) {
        return FulfillmentWarehousingAbnormalQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.whCd(),
                command.slipNo()
        );
    }

    public static FulfillmentWarehousingAbnormalImageQuery mapToAbnormalImageQuery(
            GetFulfillmentWarehousingAbnormalImageCommand command
    ) {
        return FulfillmentWarehousingAbnormalImageQuery.of(
                command.accessToken(),
                command.slipNo(),
                command.godCd(),
                command.goodsSerialNo(),
                command.fileSeq(),
                command.imgNo()
        );
    }

    public static FulfillmentWarehousingInspecDetailQuery mapToInspecDetailQuery(
            GetFulfillmentWarehousingInspecDetailCommand command
    ) {
        return FulfillmentWarehousingInspecDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNo(),
                command.whCd()
        );
    }

    public static FulfillmentWarehousingMapper mapToUpdateRequest(UpdateFulfillmentWarehousingCommand command) {
        List<FulfillmentWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FulfillmentWarehousingCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FulfillmentWarehousingMapper.update(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    private static FulfillmentWarehousingItemMapper mapItem(RegisterFulfillmentWarehousingItemCommand item) {
        List<FulfillmentWarehousingGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentWarehousingCommandToRequestMapper::mapGoods)
                .toList();

        return FulfillmentWarehousingItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.inWay(),
                item.slipNo(),
                item.parcelComp(),
                item.parcelInvoiceNo(),
                item.remark(),
                item.cstSupCd(),
                item.distTermDt(),
                item.makeDt(),
                item.preArv(),
                goods
        );
    }

    private static FulfillmentWarehousingItemMapper mapUpdateItem(UpdateFulfillmentWarehousingItemCommand item) {
        List<FulfillmentWarehousingGoodsMapper> goods = item.godCds().stream()
                .map(FulfillmentWarehousingCommandToRequestMapper::mapUpdateGoods)
                .toList();

        return FulfillmentWarehousingItemMapper.update(
                item.ordDt(),
                item.ordNo(),
                item.inWay(),
                item.slipNo(),
                item.parcelComp(),
                item.parcelInvoiceNo(),
                item.remark(),
                item.cstSupCd(),
                item.distTermDt(),
                item.makeDt(),
                item.preArv(),
                goods
        );
    }

    private static FulfillmentWarehousingGoodsMapper mapGoods(RegisterFulfillmentWarehousingGoodsCommand item) {
        return FulfillmentWarehousingGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }

    private static FulfillmentWarehousingGoodsMapper mapUpdateGoods(UpdateFulfillmentWarehousingGoodsCommand item) {
        return FulfillmentWarehousingGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }
}
