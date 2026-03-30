package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;

import java.util.List;

public class FulfillmentReturnDeliveryCommandToRequestMapper {

    public static FulfillmentReturnGodDetailQuery mapToReturnGodDetailQuery(GetFulfillmentReturnGodDetailCommand command) {
        return FulfillmentReturnGodDetailQuery.of(
                command.customerCode(),
                command.accessToken(),
                command.startDate(),
                command.endDate(),
                command.rtnSlipNoList(),
                command.whCd()
        );
    }

    public static FulfillmentReturnDeliveryMapper mapToRegisterRequest(RegisterFulfillmentReturnDeliveryCommand command) {
        List<FulfillmentReturnDeliveryItemMapper> returnDeliveryRequests = command.returnDeliveryRequests().stream()
                .map(FulfillmentReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                returnDeliveryRequests
        );
    }

    private static FulfillmentReturnDeliveryItemMapper mapItem(RegisterFulfillmentReturnDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.godCds())
                ? item.godCds().stream()
                .map(FulfillmentReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FulfillmentReturnDeliveryItemMapper.of(
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

    private static FulfillmentDeliveryGoodsMapper mapGoods(RegisterFulfillmentDeliveryGoodsCommand item) {
        return FulfillmentDeliveryGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }
}
