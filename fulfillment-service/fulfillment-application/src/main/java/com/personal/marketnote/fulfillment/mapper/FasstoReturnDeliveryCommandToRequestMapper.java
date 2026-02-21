package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryItemCommand;

import java.util.List;

public class FasstoReturnDeliveryCommandToRequestMapper {

    public static FasstoReturnGodDetailQuery mapToReturnGodDetailQuery(GetFasstoReturnGodDetailCommand command) {
        return FasstoReturnGodDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.rtnSlipNoList(),
                command.whCd()
        );
    }

    public static FasstoReturnDeliveryMapper mapToRegisterRequest(RegisterFasstoReturnDeliveryCommand command) {
        List<FasstoReturnDeliveryItemMapper> returnDeliveryRequests = command.returnDeliveryRequests().stream()
                .map(FasstoReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FasstoReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                returnDeliveryRequests
        );
    }

    private static FasstoReturnDeliveryItemMapper mapItem(RegisterFasstoReturnDeliveryItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.godCds())
                ? item.godCds().stream()
                .map(FasstoReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FasstoReturnDeliveryItemMapper.of(
                item.ordDt(),
                item.ordNo(),
                item.parcelCd(),
                item.invoiceNo(),
                item.custNm(),
                item.custTelNo(),
                item.custAddr(),
                item.rtnEmpNm(),
                item.rtnTelNo(),
                item.rtnZipCd(),
                item.rtnAddr1(),
                item.rtnAddr2(),
                item.rtnGubun(),
                item.rtnReason(),
                item.getRtnDetailReason(),
                item.rtnShipReqTerm(),
                goods
        );
    }

    private static FasstoDeliveryGoodsMapper mapGoods(RegisterFasstoDeliveryGoodsCommand item) {
        return FasstoDeliveryGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }
}
