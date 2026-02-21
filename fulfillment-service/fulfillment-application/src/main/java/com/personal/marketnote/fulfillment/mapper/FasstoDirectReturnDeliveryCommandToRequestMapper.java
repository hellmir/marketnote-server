package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoDirectReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDirectReturnDeliveryItemCommand;

import java.util.List;

public class FasstoDirectReturnDeliveryCommandToRequestMapper {

    public static FasstoDirectReturnDeliveryMapper mapToRegisterRequest(RegisterFasstoDirectReturnDeliveryCommand command) {
        List<FasstoDirectReturnDeliveryItemMapper> directReturnDeliveryRequests = command.directReturnDeliveryRequests().stream()
                .map(FasstoDirectReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FasstoDirectReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                directReturnDeliveryRequests
        );
    }

    private static FasstoDirectReturnDeliveryItemMapper mapItem(RegisterFasstoDirectReturnDeliveryItemCommand item) {
        List<FasstoDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.godCds())
                ? item.godCds().stream()
                .map(FasstoDirectReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FasstoDirectReturnDeliveryItemMapper.of(
                item.ordDt(),
                item.supCd(),
                item.orgParcelCd(),
                item.orgInvoiceNo(),
                item.inWay(),
                item.custNm(),
                item.rtnParcelComp(),
                item.rtnInvoiceNo(),
                item.rtnGubun(),
                item.rtnReason(),
                item.rtnDetailReason(),
                item.remark(),
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
