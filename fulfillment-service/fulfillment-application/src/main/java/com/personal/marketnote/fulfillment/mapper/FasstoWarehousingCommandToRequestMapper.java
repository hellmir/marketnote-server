package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.warehousing.*;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoWarehousingCommandToRequestMapper {
    public static FasstoWarehousingMapper mapToRegisterRequest(RegisterFasstoWarehousingCommand command) {
        List<FasstoWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapItem)
                .toList();

        return FasstoWarehousingMapper.register(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    public static FasstoWarehousingQuery mapToQuery(GetFasstoWarehousingCommand command) {
        return FasstoWarehousingQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.inWay(),
                command.ordNo(),
                command.wrkStat()
        );
    }

    public static FasstoWarehousingDetailQuery mapToDetailQuery(GetFasstoWarehousingDetailCommand command) {
        return FasstoWarehousingDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.slipNo(),
                command.ordNo()
        );
    }

    public static FasstoWarehousingAbnormalQuery mapToAbnormalQuery(GetFasstoWarehousingAbnormalCommand command) {
        return FasstoWarehousingAbnormalQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.whCd(),
                command.slipNo()
        );
    }

    public static FasstoWarehousingAbnormalImageQuery mapToAbnormalImageQuery(
            GetFasstoWarehousingAbnormalImageCommand command
    ) {
        return FasstoWarehousingAbnormalImageQuery.of(
                command.accessToken(),
                command.slipNo(),
                command.godCd(),
                command.goodsSerialNo(),
                command.fileSeq(),
                command.imgNo()
        );
    }

    public static FasstoWarehousingMapper mapToUpdateRequest(UpdateFasstoWarehousingCommand command) {
        List<FasstoWarehousingItemMapper> requests = command.warehousingRequests().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapUpdateItem)
                .toList();

        return FasstoWarehousingMapper.update(
                command.customerCode(),
                command.accessToken(),
                requests
        );
    }

    private static FasstoWarehousingItemMapper mapItem(RegisterFasstoWarehousingItemCommand item) {
        List<FasstoWarehousingGoodsMapper> goods = item.godCds().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapGoods)
                .toList();

        return FasstoWarehousingItemMapper.of(
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

    private static FasstoWarehousingItemMapper mapUpdateItem(UpdateFasstoWarehousingItemCommand item) {
        List<FasstoWarehousingGoodsMapper> goods = item.godCds().stream()
                .map(FasstoWarehousingCommandToRequestMapper::mapUpdateGoods)
                .toList();

        return FasstoWarehousingItemMapper.update(
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

    private static FasstoWarehousingGoodsMapper mapGoods(RegisterFasstoWarehousingGoodsCommand item) {
        return FasstoWarehousingGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }

    private static FasstoWarehousingGoodsMapper mapUpdateGoods(UpdateFasstoWarehousingGoodsCommand item) {
        return FasstoWarehousingGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }
}
