package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryGoodsMapper;
import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentDirectReturnDeliveryItemMapper;
import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;

import java.util.List;

public class FulfillmentDirectReturnDeliveryCommandToRequestMapper {

    public static FulfillmentDirectReturnDeliveryMapper mapToRegisterRequest(RegisterFulfillmentDirectReturnDeliveryCommand command) {
        List<FulfillmentDirectReturnDeliveryItemMapper> directReturnDeliveryRequests = command.directReturnDeliveryRequests().stream()
                .map(FulfillmentDirectReturnDeliveryCommandToRequestMapper::mapItem)
                .toList();

        return FulfillmentDirectReturnDeliveryMapper.register(
                command.customerCode(),
                command.accessToken(),
                directReturnDeliveryRequests
        );
    }

    private static FulfillmentDirectReturnDeliveryItemMapper mapItem(RegisterFulfillmentDirectReturnDeliveryItemCommand item) {
        List<FulfillmentDeliveryGoodsMapper> goods = FormatValidator.hasValue(item.godCds())
                ? item.godCds().stream()
                .map(FulfillmentDirectReturnDeliveryCommandToRequestMapper::mapGoods)
                .toList()
                : List.of();

        return FulfillmentDirectReturnDeliveryItemMapper.of(
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

    private static FulfillmentDeliveryGoodsMapper mapGoods(RegisterFulfillmentDeliveryGoodsCommand item) {
        return FulfillmentDeliveryGoodsMapper.of(
                item.cstGodCd(),
                item.distTermDt(),
                item.ordQty()
        );
    }
}
