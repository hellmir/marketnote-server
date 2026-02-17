package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoWarehousingGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoWarehousingRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoWarehousingGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoWarehousingRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;

import java.util.List;

public class FasstoWarehousingRequestToCommandMapper {
    public static RegisterFasstoWarehousingCommand mapToRegisterCommand(
            String customerCode,
            String accessToken,
            List<RegisterFasstoWarehousingRequest> request
    ) {
        List<RegisterFasstoWarehousingItemCommand> warehousingRequests = request.stream()
                .map(FasstoWarehousingRequestToCommandMapper::mapItem)
                .toList();

        return RegisterFasstoWarehousingCommand.of(customerCode, accessToken, warehousingRequests);
    }

    public static GetFasstoWarehousingCommand mapToWarehousingQuery(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        return GetFasstoWarehousingCommand.of(customerCode, accessToken, startDate, endDate, inWay, ordNo, wrkStat);
    }

    public static GetFasstoWarehousingDetailCommand mapToWarehousingDetailCommand(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return GetFasstoWarehousingDetailCommand.of(customerCode, accessToken, slipNo, ordNo);
    }

    public static GetFasstoWarehousingAbnormalCommand mapToWarehousingAbnormalCommand(
            String customerCode,
            String accessToken,
            String whCd,
            String slipNo
    ) {
        return GetFasstoWarehousingAbnormalCommand.of(customerCode, accessToken, whCd, slipNo);
    }

    public static GetFasstoWarehousingAbnormalImageCommand mapToWarehousingAbnormalImageCommand(
            String accessToken,
            String slipNo,
            String godCd,
            String goodsSerialNo,
            String fileSeq,
            String imgNo
    ) {
        return GetFasstoWarehousingAbnormalImageCommand.of(
                accessToken,
                slipNo,
                godCd,
                goodsSerialNo,
                fileSeq,
                imgNo
        );
    }

    public static UpdateFasstoWarehousingCommand mapToUpdateCommand(
            String customerCode,
            String accessToken,
            List<UpdateFasstoWarehousingRequest> request
    ) {
        List<UpdateFasstoWarehousingItemCommand> warehousingRequests = request.stream()
                .map(FasstoWarehousingRequestToCommandMapper::mapUpdateItem)
                .toList();

        return UpdateFasstoWarehousingCommand.of(customerCode, accessToken, warehousingRequests);
    }

    private static RegisterFasstoWarehousingItemCommand mapItem(RegisterFasstoWarehousingRequest item) {
        List<RegisterFasstoWarehousingGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoWarehousingRequestToCommandMapper::mapGoods)
                .toList();

        return RegisterFasstoWarehousingItemCommand.builder()
                .ordDt(item.getOrdDt())
                .ordNo(item.getOrdNo())
                .inWay(item.getInWay())
                .slipNo(item.getSlipNo())
                .parcelComp(item.getParcelComp())
                .parcelInvoiceNo(item.getParcelInvoiceNo())
                .remark(item.getRemark())
                .cstSupCd(item.getCstSupCd())
                .distTermDt(item.getDistTermDt())
                .makeDt(item.getMakeDt())
                .preArv(item.getPreArv())
                .godCds(goods)
                .build();
    }

    private static UpdateFasstoWarehousingItemCommand mapUpdateItem(UpdateFasstoWarehousingRequest item) {
        List<UpdateFasstoWarehousingGoodsCommand> goods = item.getGodCds().stream()
                .map(FasstoWarehousingRequestToCommandMapper::mapUpdateGoods)
                .toList();

        return UpdateFasstoWarehousingItemCommand.of(
                item.getOrdDt(),
                item.getOrdNo(),
                item.getInWay(),
                item.getSlipNo(),
                item.getParcelComp(),
                item.getParcelInvoiceNo(),
                item.getRemark(),
                item.getCstSupCd(),
                item.getDistTermDt(),
                item.getMakeDt(),
                item.getPreArv(),
                goods
        );
    }

    private static RegisterFasstoWarehousingGoodsCommand mapGoods(RegisterFasstoWarehousingGoodsRequest item) {
        return RegisterFasstoWarehousingGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }

    private static UpdateFasstoWarehousingGoodsCommand mapUpdateGoods(UpdateFasstoWarehousingGoodsRequest item) {
        return UpdateFasstoWarehousingGoodsCommand.of(
                item.getCstGodCd(),
                item.getDistTermDt(),
                item.getOrdQty()
        );
    }
}
